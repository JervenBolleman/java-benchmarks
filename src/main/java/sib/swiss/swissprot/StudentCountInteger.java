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

/** Uses Class.hashCode to select a Counter to increment. Aim is to test that Class.hashCode is no
 * slower between a Graal enabled JVM and an HotSpot one. **/
package sib.swiss.swissprot;

import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;


@State(Scope.Thread)
public class StudentCountInteger
{
	Student[] students = new Student[2000];

	@Setup
	public void setUp()
	{
		Random r = new Random(42);
		for (int i = 0; i < students.length; i++)
		{
			boolean b = r.nextBoolean();
			if (b)
				students[i] = new Student(1);
			else
				students[i] = new Student(0);
		}
	}

	@Benchmark
	public int countMales()
	{
		int males = 0;
		for (int i = 0; i < students.length; i++)
		{
			males = males + students[i].genderCode;
		}
		return males;
	}

	@Benchmark
	public int countFemales()
	{
		int females = students.length;
		for (int i = 0; i < students.length; i++)
		{
			females = females - students[i].genderCode;
		}
		return females;
	}
	
	@Benchmark
	public int countBoth()
	{
		int females = students.length;
		int males = 0;
		for (int i = 0; i < students.length; i++)
		{
			females = females - students[i].genderCode;
			males = males + students[i].genderCode;
		}
		return females + males;
	}

	private class Student
	{
		public Student(int genderCode)
		{
			this.genderCode = genderCode;
		}

		int genderCode;
	}
	
	public static void main(String args[]) {
		for (int i = 0; i < 10_000; i++) {
			final StudentCountInteger scc = new StudentCountInteger ();
			scc.setUp();
			System.err.println((scc.countBoth() == scc.students.length) + " all students are male or female");
			System.err.println((scc.countMales() == scc.students.length) + " all students are male");
			System.err.println((scc.countFemales() == scc.students.length) + " all students are female");
		}
	}
}
