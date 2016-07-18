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

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@State(Scope.Thread)
public class ClassNameHashCode
{

	private static final int ITERATIONS = 5;
	private static final int INVOCATIONS = 50_000_000;
	private Class<?>[] classes;
	private static final Random random = new Random();
	private Map<String, Counter> classViaStringCount;

	@Setup
	public void setUp()
	{
		List<Class<?>> cl = new ArrayList<>();
		cl.add(Boolean.class);
		cl.add(Byte.class);
		cl.add(Character.class);
		cl.add(Character.Subset.class);
		cl.add(Character.UnicodeBlock.class);
		cl.add(Class.class);
		cl.add(ClassLoader.class);
		cl.add(ClassValue.class);
		cl.add(Compiler.class);
		cl.add(Double.class);
		cl.add(Enum.class);
		cl.add(Float.class);
		cl.add(InheritableThreadLocal.class);
		cl.add(Integer.class);
		cl.add(Long.class);
		cl.add(Math.class);
		cl.add(Number.class);
		cl.add(Object.class);
		cl.add(Package.class);
		cl.add(Process.class);
		cl.add(ProcessBuilder.class);
		cl.add(ProcessBuilder.Redirect.class);
		cl.add(Runtime.class);
		cl.add(RuntimePermission.class);
		cl.add(SecurityManager.class);
		cl.add(Short.class);
		cl.add(StackTraceElement.class);
		cl.add(StrictMath.class);
		cl.add(String.class);
		cl.add(StringBuffer.class);
		cl.add(StringBuilder.class);
		cl.add(System.class);
		cl.add(Thread.class);
		cl.add(ThreadGroup.class);
		cl.add(ThreadLocal.class);
		cl.add(Throwable.class);
		cl.add(Void.class);

		cl.add(Character.UnicodeScript.class);
		cl.add(ProcessBuilder.Redirect.Type.class);
		cl.add(Thread.State.class);

		cl.add(ArithmeticException.class);
		cl.add(ArrayIndexOutOfBoundsException.class);
		cl.add(ArrayStoreException.class);
		cl.add(ClassCastException.class);
		cl.add(ClassNotFoundException.class);
		cl.add(CloneNotSupportedException.class);
		cl.add(EnumConstantNotPresentException.class);
		cl.add(Exception.class);
		cl.add(IllegalAccessException.class);
		cl.add(IllegalArgumentException.class);
		cl.add(IllegalMonitorStateException.class);
		cl.add(IllegalStateException.class);
		cl.add(IllegalThreadStateException.class);
		cl.add(IndexOutOfBoundsException.class);
		cl.add(InstantiationException.class);
		cl.add(InterruptedException.class);
		cl.add(NegativeArraySizeException.class);
		cl.add(NoSuchFieldException.class);
		cl.add(NoSuchMethodException.class);
		cl.add(NullPointerException.class);
		cl.add(NumberFormatException.class);
		cl.add(ReflectiveOperationException.class);
		cl.add(RuntimeException.class);
		cl.add(SecurityException.class);
		cl.add(StringIndexOutOfBoundsException.class);
		cl.add(TypeNotPresentException.class);
		cl.add(UnsupportedOperationException.class);

		cl.add(AbstractMethodError.class);
		cl.add(AssertionError.class);
		cl.add(BootstrapMethodError.class);
		cl.add(ClassCircularityError.class);
		cl.add(ClassFormatError.class);
		cl.add(Error.class);
		cl.add(ExceptionInInitializerError.class);
		cl.add(IllegalAccessError.class);
		cl.add(IncompatibleClassChangeError.class);
		cl.add(InstantiationError.class);
		cl.add(InternalError.class);
		cl.add(LinkageError.class);
		cl.add(NoClassDefFoundError.class);
		cl.add(NoSuchFieldError.class);
		cl.add(NoSuchMethodError.class);
		cl.add(OutOfMemoryError.class);
		cl.add(StackOverflowError.class);
		cl.add(ThreadDeath.class);
		cl.add(UnknownError.class);
		cl.add(UnsatisfiedLinkError.class);
		cl.add(UnsupportedClassVersionError.class);
		cl.add(VerifyError.class);
		cl.add(VirtualMachineError.class);

		cl.add(Deprecated.class);
		cl.add(Override.class);
		cl.add(SafeVarargs.class);
		cl.add(SuppressWarnings.class);
		classes = cl.toArray(new Class<?>[] {});
		classViaStringCount = new HashMap<>();
		for (Class<?> clazz : classes)
		{
			classViaStringCount.put(clazz.getName(), new Counter());
		}
	}

	@TearDown
	public void check()
	{
		final int invocations2 = classViaStringCount.values().stream()
		    .mapToInt(Counter::getCount).sum();
		assert invocations2 == ITERATIONS * 2 * INVOCATIONS || invocations2 == 0 : "expected " + INVOCATIONS + " got "
		    + invocations2;
		classViaStringCount.clear();
	}

	@Benchmark
	@Warmup(iterations = ITERATIONS, batchSize = INVOCATIONS)
	@Measurement(iterations = 5, batchSize = INVOCATIONS)
	@BenchmarkMode(Mode.SingleShotTime)
	public void countClassSeenViaRandomSelection()
	{
		Class<?> classToUse = classes[random.nextInt(classes.length)];
		final Counter counter = classViaStringCount.get(classToUse.getName());
		counter.run();
	}

	static class Counter
	{
		private int count = 0;

		void run()
		{
			count = count + 1;
		}

		int getCount()
		{
			return count;
		}

	}
}
