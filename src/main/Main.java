package main;

import com.martiansoftware.jsap.JSAPResult;
import java.util.concurrent.Callable;
import java.lang.Integer;

public class Main implements Callable<Integer> {
	/**
	 * @param args
	 * @throws JSAPException
	 */
	public static void main(String[] args) throws Exception {
		ArgumentParser aparser = new ArgumentParser(args);
		if (!aparser.Parse()) {
			aparser.PrintParseErrors();
			System.exit(1);
		}
		Main m = new Main(aparser.config);
		Integer ret = m.call();
		System.out.println("Main.call() returned " + ret.toString());
	}

	/***********************************************************/

	private JSAPResult config;

	public Main(JSAPResult config) {
		this.config = config;
	}

	@Override
	public Integer call() throws Exception {
		String logfile = this.config.getString("logfile");
		Boolean print_invariants = this.config.getBoolean("print_invariants");
		String[] reg_exp = this.config.getStringArray("reg_exp");

		System.out.println("logfile: " + logfile);
		System.out.println("print_invs: " + print_invariants.toString());
		System.out.println("reg_exp: " + reg_exp.toString());
		
		// TODO: do something here

		return new Integer(0);
	}

}
