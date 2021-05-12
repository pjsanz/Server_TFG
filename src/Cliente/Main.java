package Cliente;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if (args.length > 1 || args.length == 0) {
			System.err.println("ERROR : Introduce <maquina:puerto>");
			System.exit(-1);
		}
		
		String argumento[] = args[0].split(":");
		int puerto = Integer.parseInt(argumento[1]);
		String maquina = argumento[0];
		
		Cliente c = new Cliente(maquina, puerto);
		c.inicio();
	}

}
