package Cliente;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import Entidades.Coordenadas;
import Entidades.DatosCliente;
import Entidades.LittleEndian;
import Entidades.TipoMensaje;
import Peticiones.Autenticacion;
import Peticiones.CerrarSesion;
import Peticiones.Colision;
import Peticiones.EnvioCoordCliente;
import Peticiones.EnvioCoordServidor;
import Peticiones.HistoricoPuntuaciones;
import Peticiones.InicioPartida;
import Peticiones.RespuestaAutenticacion;
import Peticiones.RespuestaHistoricoPuntuaciones;

public class Cliente {

	private Socket s = null;
	private BufferedReader bf;
	private DatosCliente misDatos;
	private ArrayList<DatosCliente> listaDatosRivales;
	boolean logeado;
	boolean juegoIniciado;

	public Cliente(String maquina, int puerto){

		try {
			this.s = new Socket(maquina,puerto);
		} catch (UnknownHostException e) {
			System.out.println(e);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public void inicio() throws Exception{

		logeado = false;

		listaDatosRivales = new ArrayList<DatosCliente>();

		System.out.println("Tipos de Peticiones cliente (App Android):");	
		System.out.println("0 - Autenticacion en el sistema: 0 + usuario + password");
		System.out.println("1 - Inicio partida: 2 ");
		System.out.println("3 - Historico de puntuaciones: 7 ");
		System.out.println("4 - CerrarSesion: 9 ");


		try {

			bf = new BufferedReader(new InputStreamReader(System.in));

			BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
			DataOutputStream dos = new DataOutputStream(bos);

			BufferedInputStream bis = new BufferedInputStream(this.s.getInputStream());
			DataInputStream dis = new DataInputStream(bis);

			while(true){
				
				String linea = bf.readLine();
		
				if (linea!=null){					
					RealizarPeticion(linea,dis, dos);						
				}																																														
			}

		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}


	}

	private void RealizarPeticion(String linea, DataInputStream dis, DataOutputStream dos) throws Exception {

		String mensaje[] = linea.split(" ");	
		int cabeceraMensaje = Integer.parseInt(mensaje[0]);

		try {

			switch (cabeceraMensaje) {

				case 0:

					if (!logeado){

						Autenticacion peticion = new Autenticacion(mensaje[1], mensaje[2]);		
						peticion.aplanar(dos);

						ObtenerRespuestaAutenticacion(linea,dis, dos);			

					}else{
							
						System.err.println("Ya estas logeado");
					}
					break;		

				case 2:

					if (logeado){

						//Parte de coordenadas sustituir en app por las coordenadas actuales

						Integer latitudCoord = new Random().nextInt(6);
						Integer longitudCoord = new Random().nextInt(6);

						String ejemploCoord = latitudCoord.toString() + "," +  longitudCoord.toString();

						InicioPartida peticion = new InicioPartida(misDatos.getSesion(), ejemploCoord);
						peticion.aplanar(dos);
						
						ObtenerRespuestaInicio(dis, dos);
		
					}else{								
						System.err.println("Debes estar logeado para realizar esta acción");
					}

					break;
																																							
				case 7:

					if (logeado){

						HistoricoPuntuaciones peticion = new HistoricoPuntuaciones(misDatos.getSesion());													
						peticion.aplanar(dos);		

						ObtenerRespuestaHistorico(linea,dis);

					}else{
														
						System.err.println("Debes estar logeado para realizar esta acción");
					}
					break;	
				
				case 8:

					if (logeado){

						CerrarSesion peticion = new CerrarSesion(misDatos.getSesion());													
						peticion.aplanar(dos);		

						logeado = false;

					}else{						
						System.err.println("Debes estar logeado para realizar esta acción");
					}
					break;	
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Peticion realizada incorrectamente");
		}
	}

	private void ObtenerRespuestaAutenticacion(String linea, DataInputStream dis, DataOutputStream dos) throws IOException, InterruptedException {

		byte [] bytes = new byte[4];
		int n = -1;
	
		n = dis.read(bytes);

		if (n!=-1){

			RespuestaAutenticacion respuestaPeticion = RespuestaAutenticacion.desaplanar(dis);

			if (respuestaPeticion.getRespuesta().equals("OK") || respuestaPeticion.getRespuesta().equals("Registro")){

				System.err.println("Autenticacion completada correctamente!!!");

				logeado = true;
				
				misDatos = new DatosCliente(linea.split(" ")[1]);
				
				misDatos.setSesion(respuestaPeticion.getIdSesion());
				
				System.err.println("Usuario: " + misDatos.getUsuario());
				System.err.println("Id Sesion: " + misDatos.getSesion());

			}
			else if (respuestaPeticion.getRespuesta().equals("KO")){
				
				System.err.println("Password incorrecto, usuario ya existente!");					
			}
			else if (respuestaPeticion.getRespuesta().equals("duplicado")){
				
				System.err.println("El usuario ya ha iniciado sesion!");					
			}
			else {
				System.err.println("Error al realizar la autenticacion");		
			}			
		}		
	}
	
	private void ObtenerRespuestaInicio(DataInputStream dis, DataOutputStream dos) throws IOException, InterruptedException {
		
		//Aqui tengo que recibir las coordenadas por parte del servidor y enviar las primeras mias
						
		byte [] bytes = new byte[4];
		int n = -1;
		
		n = dis.read(bytes);
		
		if (n!=-1){
										
			EnvioCoordServidor respuestaPeticion = EnvioCoordServidor.desaplanar(dis);
									
			System.err.println(respuestaPeticion.getUsuarios());
			System.err.println(respuestaPeticion.getCoordenadas());
			
			//Parte de coordenadas sustituir en app por las coordenadas actuales
			
			InsertarCoordenadasUsuariosListaRivales(respuestaPeticion);
																					
			Integer latitudCoord = new Random().nextInt(6);
			Integer longitudCoord = new Random().nextInt(6);
			
			String ejemploCoord = latitudCoord.toString() + "," +  longitudCoord.toString();
			
			EnvioCoordCliente peticion = new EnvioCoordCliente(misDatos.getSesion(), ejemploCoord);											
			
			peticion.aplanar(dos);	
			 
			//Hemos iniciado el juego 
			
			PartidaIniciada(dis, dos);
			
			//Pintamos nuestra coordenada enviada
															
		}
																													
		
	}
	
	private void PartidaIniciada(DataInputStream dis, DataOutputStream dos) throws InterruptedException, IOException {
		
		// Como hemos iniciado el juego ya nos quedamos leyendo y intercambiando coordenadas
		// Hasta que se produzca una colision
		
		//int contador = 0;
			
		byte [] bytes = new byte[4];
		int n = -1;
		boolean juegoIniciado = true;
		
		while(juegoIniciado){
			
			Thread.sleep(2000);
			
			bytes = new byte[4];
			n = -1;
			
			n = dis.read(bytes);
			
			if (n!=-1){
			
				TipoMensaje tipo = TipoMensaje.values()[LittleEndian.desempaquetar(bytes)];
				
				switch (tipo) {
				
					case EnvioCoordServidor:
						
						EnvioCoordServidor respuestaPeticion2 = EnvioCoordServidor.desaplanar(dis);
						System.err.println(respuestaPeticion2.getUsuarios());
						System.err.println(respuestaPeticion2.getCoordenadas());

						InsertarCoordenadasUsuariosListaRivales(respuestaPeticion2);
						
						Integer latitudCoord = new Random().nextInt(6);
						Integer longitudCoord = new Random().nextInt(6);
						
						String ejemploCoord = latitudCoord.toString() + "," +  longitudCoord.toString();
						
						EnvioCoordCliente peticion = new EnvioCoordCliente(misDatos.getSesion(), ejemploCoord);											
						
						peticion.aplanar(dos);					
						
						//Pintamos nuestra coordenada enviada
						
					break;
					
					case Colision:
						
						Colision peticionColision = Colision.desaplanar(dis);					
						
						System.err.println("Has colisionado con la estela de: " + peticionColision.getUsuarioColision());
						System.err.println("Has obtenido una puntuacion de: " + peticionColision.getPuntuacion());

						//Reiniciamos los parametros del juego para el cliente
						
						listaDatosRivales = new ArrayList<DatosCliente>();
						misDatos.setCoordenadas(new ArrayList<Coordenadas>());
						juegoIniciado = false;
						
						break;
						
					default:
						break;
				}																										
				
			}
		}
	}
	
	private void ObtenerRespuestaHistorico(String linea, DataInputStream dis) throws IOException {

		byte [] bytes = new byte[4];
		int n = -1;

		n = dis.read(bytes);

		if (n!=-1){

			RespuestaHistoricoPuntuaciones respuestaPeticion = RespuestaHistoricoPuntuaciones.desaplanar(dis);

			String[] puntuaciones = respuestaPeticion.getPuntuaciones().split("@");

			System.err.println("Puntos | UsuarioColision | fecha");

			for (String puntos : puntuaciones) {					        
				System.err.println(puntos);
		    }
							
		}							
	}
	
	private void InsertarCoordenadasUsuariosListaRivales(EnvioCoordServidor respuestaPeticion) {

		try {
							
		//Descompongo y las anado a la lista de usuarios si esta esta vacia quiere decir 
		//que he recibido un primer mensaje del servidor con los usuarios activos

		//Anadimos usuarios menos nosotros mismos!

		DatosCliente datos = null;

		String[] usuarios = respuestaPeticion.getUsuarios().split("&");
		String[] coordenadas = respuestaPeticion.getCoordenadas().split("@");
	
		Boolean existeUsuario = false;
		int     indiceUsuario = 0;
			
		for(int i = 0; i < usuarios.length; i++) {
		
			if(!usuarios[i].equals(misDatos.getUsuario())) {

				//Comprobamos si el usuario ya estaba anadido para no anadirle de nuevo
				//Solamente introducimos su ultima coordenada

				for (DatosCliente datosClienteUsu : listaDatosRivales) {
					if(datosClienteUsu.getUsuario().equals(usuarios[i])) {
						existeUsuario = true;
						indiceUsuario = listaDatosRivales.indexOf(datosClienteUsu);
						break;
					}
				}

				if(!existeUsuario) {
					 datos = new DatosCliente(usuarios[i]);
					 datos.insertarCoordenadas(coordenadas[i]);
					 listaDatosRivales.add(datos);
				}
				else {
					datos = listaDatosRivales.get(indiceUsuario);
					datos.insertarCoordenadas(coordenadas[i]);

					listaDatosRivales.set(indiceUsuario, datos);
				}

			}
			
			//Eliminamos los usuarios de la lista por si alguno ha desaparecido ya no es rival	
			
			EliminarUsuarioLista(usuarios);
		
		}
		
		//PINTAMOS COORDENADAS EN EL MAPA EN LA APP DE ANDROID las coordenadas de los usuarios que me llegan 
		}
		catch(IndexOutOfBoundsException e) {
						
		}			
	}
	
	private void EliminarUsuarioLista(String[] usuarios) {
		for (String usuario : usuarios) {
			if(!EncontrarUsuarioLista(usuarios, usuario)) {
				EliminarRival(usuario);
			}
		}	
	}
	
	private boolean EncontrarUsuarioLista(String[] usuarios, String usuario) {

		boolean retorno = false;

		for(String usu : usuarios) {
			if(usu.equals(usuario)) {
				retorno = true;
				break;
			}
		}

		return retorno;

	}
	
	private void EliminarRival(String usuario) {

		int indiceUsuario = 0;

		for (DatosCliente datosClienteUsu : listaDatosRivales) {
			if(datosClienteUsu.getUsuario().equals(usuario)) {
				indiceUsuario = listaDatosRivales.indexOf(datosClienteUsu);
				break;
			}
		}

		listaDatosRivales.remove(indiceUsuario);

	}
}
