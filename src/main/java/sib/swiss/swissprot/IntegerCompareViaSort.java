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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;


@State(Scope.Thread)
public class IntegerCompareViaSort
{
	private static final int size = 2_000;
	private List<Integer> list = new ArrayList<>(size);

	@Setup
	public void setUp()
	{
		for (int i = 0; i < size; i++)
		{
			list.add(i);
		}
		Collections.shuffle(list);
	}

	@Benchmark
	public int integerCompareSortThenCount()
	{
		list.sort(IntegerCompareViaSort::compareByBranches);
		return list.stream().mapToInt(Integer::intValue).sum();
	}

	@Benchmark
	public int aMinusBCompareSortThenCount()
	{
		list.sort(IntegerCompareViaSort::compareBySubtraction);
		return list.stream().mapToInt(Integer::intValue).sum();
	}

	public static int compareBySubtraction(int x, int y)
	{
		return x - y;
	}

	public static int compareByBranches(int x, int y)
	{
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}
}
