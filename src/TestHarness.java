import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.TreeSet;

public class TestHarness {
	public static void main(String[] args) {
		new TestHarness();
	}

	ArrayList<Test> tests = new ArrayList<>();
	ArrayList<Category> categories = new ArrayList<>();
	ArrayList<Program> programs = new ArrayList<>();
	ArrayDeque<Thread> runningTasks = new ArrayDeque<>();

	TestHarness() {
		Category basic = addCategory("basic");
		Category bigfiles = addCategory("bigfiles");

		addTest("anatomy.mm", true, "Simple 'anatomy' test", basic);
		addTest("anatomy-bad1.mm", false, "Simple incorrect 'anatomy' test ", basic);
		addTest("anatomy-bad2.mm", false, "Simple incorrect 'anatomy' test ", basic);
		addTest("anatomy-bad3.mm", false, "Simple incorrect 'anatomy' test ", basic);
		addTest("big-unifier.mm", true, null, basic);
		addTest("big-unifier-bad1.mm", false, null, basic);
		addTest("big-unifier-bad2.mm", false, null, basic);
		addTest("big-unifier-bad3.mm", false, null, basic);
		addTest("demo0.mm", true, null, basic);
		addTest("demo0-bad1.mm", false, null, basic);
		addTest("demo0-includer.mm", true, "Test simple file inclusion");
		addTest("emptyline.mm", true, "A file with one empty line");
		addTest("hol.mm", true, null, bigfiles);
		addTest("iset.mm", true, null, bigfiles);
		addTest("miu.mm", true, null);
		addTest("nf.mm", true, null, bigfiles);
		addTest("peano-fixed.mm", true, null);
		addTest("ql.mm", true, null, bigfiles);
		addTest("set.2010-08-29.mm", true, null, bigfiles);
		addTest("set.mm", true, null, bigfiles);

		Category all = addCategory("all tests");
		for (Test t : tests)
			all.add(t);

		programs.add(new Program("metamath", all));
		programs.add(new Program("smetamath", all));
		programs.add(new Program("checkmm", all));
		programs.add(new Program("mmj2", all));
		programs.add(new Program("mmverifypy", all));

		runAllTests(false);

		boolean allSuccess = true;
		for (Program p : programs) {
			for (Test t : p.expected) {
				if (!p.getTestResult(t)) {
					allSuccess = false;
					System.err.println(p.testOutput[t.index]);
				}
			}
		}

		System.exit(allSuccess ? 0 : 1);
	}

	void runAllTests(boolean parallel) {
		if (parallel) {
			for (Program p : programs) {
				System.out.println("# Running tests for " + p.name);
				System.out.println("1.." + tests.size());
				for (Test t : tests) {
					Thread thread = new Thread(() -> p.printTestResult(t, p.test(t)));
					thread.start();
					runningTasks.add(thread);
				}
			}
			while (!runningTasks.isEmpty()) {
				Thread t = runningTasks.pop();
				try {
					t.join();
				} catch (InterruptedException e) {
					runningTasks.push(t);
				}
			}
		} else {
			for (Program p : programs) {
				System.out.println("# Running tests for " + p.name);
				System.out.println("1.." + tests.size());
				for (Test t : tests)
					p.printTestResult(t, p.test(t));
			}
		}
	}

	Test addTest(String file, boolean pass, String desc, Category... cats) {
		Test t = new Test(tests.size(), file, pass, desc);
		for (Category c : cats)
			c.add(t);
		tests.add(t);
		return t;
	}

	Category addCategory(String name, Category... deps) {
		Category out = new Category(categories.size(), name, deps);
		categories.add(out);
		return out;
	}

	public class Test implements Comparable<Test> {
		public final int index;
		public final String file;
		public final String desc;
		public final boolean pass;

		public Test(int index, String file, boolean pass, String desc) {
			this.index = index;
			this.file = file;
			this.desc = desc;
			this.pass = pass;
		}

		@Override
		public int compareTo(Test t) {
			return index - t.index;
		}
	}

	public class Category {
		public final int index;
		public final String name;
		public TreeSet<Test> tests = new TreeSet<>();
		public final Category[] deps;

		public Category(int index, String name, Category... deps) {
			this.index = index;
			this.name = name;
			this.deps = deps;
			for (Category c : deps)
				tests.addAll(c.tests);
		}

		public void add(Test t) {
			tests.add(t);
			for (Category c : deps)
				c.add(t);
		}
	}

	public class Program {
		public final String name;
		public final boolean[] testResults = new boolean[tests.size()];
		public final String[] testOutput = new String[tests.size()];
		public final Boolean[] catResults = new Boolean[categories.size()];
		public final TreeSet<Test> expected = new TreeSet<>();

		public Program(String name, Category... expected) {
			this.name = name;
			for (Category c : expected)
				this.expected.addAll(c.tests);
		}

		public void runTests() {
			System.out.println("# Running tests for " + name);
			System.out.println("1.." + tests.size());
		}

		public boolean getTestResult(Test t) {
			return testResults[t.index];
		}

		public boolean getCatResult(Category c) {
			Boolean out = catResults[c.index];
			if (out != null)
				return out.booleanValue();
			boolean b = true;
			for (Test t : c.tests)
				b &= getTestResult(t);
			catResults[c.index] = Boolean.valueOf(b);
			return b;
		}

		public void printTestResult(Test t, boolean result) {
			System.out.println((result ? "ok" : "not ok") + " " + t.index + " - " + name + " - " + t.file
					+ (t.desc == null ? "" : ": " + t.desc));
		}

		public boolean test(Test test) {
			boolean result = false;
			try {
				Process p = new ProcessBuilder("bash", "./test-" + name, test.file).redirectErrorStream(true).start();
				int exitCode = p.waitFor();
				String output;
				try (java.util.Scanner s = new java.util.Scanner(p.getInputStream())) {
					output = s.useDelimiter("\\A").hasNext() ? s.next() : "";
				}
				result = (exitCode == 0) == test.pass;
				synchronized (testOutput) {
					testOutput[test.index] = "Output for test " + name + " - " + test.file + ":\n" + output;
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			} finally {
				synchronized (testResults) {
					testResults[test.index] = result;
				}
			}
			return result;
		}
	}
}
