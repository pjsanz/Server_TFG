package Servidor;

public class Main {

	public static void main(String[] args) {
		
		if (args.length > 1){
			System.err.println("ERROR : Introduce <puerto>");
			System.exit(-1);
		}
		
		int puerto = Integer.parseInt(args[0]);
		
		Servidor srv = new Servidor(puerto);
		srv.inicio();

	}

}
