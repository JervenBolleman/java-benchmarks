/* Copyright (c) 2016, Swiss Institute of Bioinformatics. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

/** There are two benchmark loops here. The first is a simple counting loop, the second uses an
 * array lookup to avoid a unpredicatable branch.
 *
 * However, the first if statements need not branch either. On X86 they could be compiled to
 * something like this in asm cmp edi, 1 ; compare b to 1, assuming b is stored edi lafh ; puts the
 * flags in the AH register. shr 6, eax ; move the 6th bit to the end not eax ; if equals then the
 * ZF = 0 but we need it to be one, ; don't care about the other bits and eax, 1 ; if the 6th bit is
 * true then we have 1 set else 0. ; promote AH to the full eax register as well add eax, r13; Add
 * the 1 if true or 0 if false in the CMP to the ; destination register r13. If this compilation is
 * done the first method could even be faster than the second. **/
package sib.swiss.swissprot;

import org.openjdk.jmh.annotations.*;
import java.nio.ByteBuffer;
import java.util.Random;


@State(Scope.Thread)
public class GuanineCytosineCount
{
	private static final char ANY_NUCLEOTIDE = 'n';
	private static final char GUANINE = 'g';
	private static final char THREOSINE = 't';
	private static final char CYTOSINE = 'c';
	private static final char ADENINE = 'a';
	private static final int size = 2 * 1024 * 10;
	private ByteBuffer dna;

	@Setup
	public void setUp()
	{
		dna = ByteBuffer.allocateDirect(size);
		Random random = new Random();
		for (int i = 0; i < size; i++)
		{
			float next = random.nextFloat();
			if (next < 0.3)
			{
				dna.put((byte) 'a');
			}
			else if (next < 0.3)
			{
				dna.put((byte) 't');
			}
			else if (next < 0.8)
			{
				dna.put((byte) 'c');
			}
			else if (next < 0.99)
			{
				dna.put((byte) 'g');
			}
			else
			{
				dna.put((byte) 'n');
			}
		}
	}

	@TearDown
	public void tearDown()
	{
		dna = null;
	}

	@Benchmark
	public int countUsingSimpleIfStatements()
	{
		int a = 0, c = 0, g = 0, t = 0, n = 0;
		for (int i = 0; i < size; i++)
		{
			int nucleotide = dna.get(i);

			if (nucleotide == ADENINE)
				a++;
			if (nucleotide == CYTOSINE)
				c++;
			if (nucleotide == THREOSINE)
				t++;
			if (nucleotide == GUANINE)
				g++;
			if (nucleotide == ANY_NUCLEOTIDE)
				n++;
		}
		int count = a + c + g + t + n;
		assert count == size : "count should equals the array size";
		return count;
	}

	@Benchmark
	public int countUsingSimpleBooleanToIntConversionStatements()
	{
		int a = 0, c = 0, g = 0, t = 0, n = 0;
		for (int i = 0; i < size; i++)
		{
			int nucleotide = dna.get(i);
			a += (nucleotide == ADENINE) ? 1 : 0;
			c += (nucleotide == CYTOSINE) ? 1 : 0;
			t += (nucleotide == THREOSINE) ? 1 : 0;
			g += (nucleotide == GUANINE) ? 1 : 0;
			n += (nucleotide == ANY_NUCLEOTIDE) ? 1 : 0;
		}
		int count = a + c + g + t + n;
		assert count == size : "count should equals the array size";
		return count;
	}

	@Benchmark
	public int countUsingSwitchStatements()
	{
		int a = 0, c = 0, g = 0, t = 0, n = 0;
		for (int i = 0; i < size; i++)
		{
			int nucleotide = dna.get(i);
			switch (nucleotide)
			{
				case ADENINE:
					a++;
					break;
				case CYTOSINE:
					c++;
					break;
				case THREOSINE:
					t++;
					break;
				case GUANINE:
					g++;
					break;
				case ANY_NUCLEOTIDE:
					n++;
					break;
			}
		}
		int count = a + c + g + t + n;
		assert count == size : "count should equals the array size";
		return count;
	}

	@Benchmark
	public int countUsingAnArray()
	{
		int[] nucleotides = new int[256];
		for (int i = 0; i < size; i++)
		{
			int nucleotide = dna.get(i);
			nucleotides[nucleotide]++;
		}
		int count = nucleotides['a'] + nucleotides['c'] + nucleotides['g'] + nucleotides['t'] + nucleotides['n'];
		assert count == size : "count should equals the array size";
		return count;
	}
}
