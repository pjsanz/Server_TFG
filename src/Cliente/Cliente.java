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
	private String idSesion;	
	private String miUsuario;
	private ArrayList<DatosCliente> listaDatosRivales;
	
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
		
		boolean logeado         = false;
		boolean peticionEnviada = false;
		boolean juegoIniciado   = false;
		
		miUsuario 				= "";
		listaDatosRivales 	    = new ArrayList<DatosCliente>();
		
		byte [] bytes;
		int n;
		
		
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
				
					String mensaje[] = linea.split(" ");
					
					int cabeceraMensaje = Integer.parseInt(mensaje[0]);
					try {
					
					switch (cabeceraMensaje) {
					
						case 0:
							
							if (!logeado){
								
								Autenticacion peticion = new Autenticacion(mensaje[1], mensaje[2]);
								peticion.aplanar(dos);
								miUsuario = mensaje[1];
								peticionEnviada=true;
								
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
								
								InicioPartida peticion = new InicioPartida(idSesion, ejemploCoord);											
								
								peticion.aplanar(dos);						
								
								peticionEnviada=true;
	
							}else{								
								System.err.println("Debes estar logeado para realizar esta acción");
							}
							
							break;
																																														
						case 7:
						
							if (logeado){
								
								HistoricoPuntuaciones peticion = new HistoricoPuntuaciones(idSesion);													
								peticion.aplanar(dos);		
								
								peticionEnviada=true;
								
							}else{
																
								System.err.println("Debes estar logeado para realizar esta acción");
							}
							break;	
						
						case 8:

							if (logeado){
								
								CerrarSesion peticion = new CerrarSesion(idSesion);													
								peticion.aplanar(dos);		
								
								logeado 	    = false;
								peticionEnviada = true;
							
							}else{						
								System.err.println("Debes estar logeado para realizar esta acción");
							}
							break;	
						}
										
						
						if ((peticionEnviada)&&(!logeado)&&(mensaje[0].equals("0"))){ // Si el mensaje es 0 se que he el login
							
							bytes = new byte[4];
							n = -1;
							
							n = dis.read(bytes);
							
							if (n!=-1){
								
								RespuestaAutenticacion respuestaPeticion = RespuestaAutenticacion.desaplanar(dis);
								
								if (respuestaPeticion.getRespuesta().equals("OK")){
									
									System.out.println("Autenticaci�n completada correctamente!!!");
	
									logeado=true;
									miUsuario = mensaje[1];
									idSesion = respuestaPeticion.getIdSesion();
									System.out.println("Usuario: " + miUsuario);
									System.out.println("Id Sesion: " + idSesion);
									
								}
								else if (respuestaPeticion.getRespuesta().equals("KO")){
									
									System.out.println("Contrase�a incorrecta, nick ya existente!");					
								}
								else if (respuestaPeticion.getRespuesta().equals("duplicado")){
									
									System.out.println("El usuario ya ha iniciado sesion!");					
								}
								else {
									System.out.println("Error al realizar la autenticaci�n");		
								}
								
							}
						}
						
						//Aqui tengo que recibir las coordenadas por parte del servidor y enviar las primeras mias
							
						else if(!juegoIniciado && logeado && peticionEnviada && (mensaje[0].equals("2"))) {
							
							bytes = new byte[4];
							n = -1;
							
							n = dis.read(bytes);
							
							if (n!=-1){
															
								EnvioCoordServidor respuestaPeticion = EnvioCoordServidor.desaplanar(dis);
														
								System.out.println(respuestaPeticion.getUsuarios());
								System.out.println(respuestaPeticion.getCoordenadas());
								
								//Parte de coordenadas sustituir en app por las coordenadas actuales
								
								InsertarCoordenadasUsuariosListaRivales(respuestaPeticion);
																										
								Integer latitudCoord = new Random().nextInt(6);
								Integer longitudCoord = new Random().nextInt(6);
								
								String ejemploCoord = latitudCoord.toString() + "," +  longitudCoord.toString();
								
								EnvioCoordCliente peticion = new EnvioCoordCliente(idSesion, ejemploCoord);											
								
								peticion.aplanar(dos);	
								 
								//Hemos iniciado el juego 
								
								//Pintamos nuestra coordenada enviada
								
								juegoIniciado = true;	
																	
							}
																																		
							// Como hemos iniciado el juego ya nos quedamos leyendo y intercambiando coordenadas
							// Hasta que se produzca una colision
							
							//int contador = 0;
							
							while(juegoIniciado){
								
								Thread.sleep(3000);
								
								bytes = new byte[4];
								n = -1;
								
								n = dis.read(bytes);
								
								if (n!=-1){
								
									TipoMensaje tipo = TipoMensaje.values()[LittleEndian.desempaquetar(bytes)];
									
									switch (tipo) {
									
										case EnvioCoordServidor:
											
											EnvioCoordServidor respuestaPeticion2 = EnvioCoordServidor.desaplanar(dis);
											System.out.println(respuestaPeticion2.getUsuarios());
											System.out.println(respuestaPeticion2.getCoordenadas());

											InsertarCoordenadasUsuariosListaRivales(respuestaPeticion2);
											
											Integer latitudCoord = new Random().nextInt(6);
											Integer longitudCoord = new Random().nextInt(6);
											
											String ejemploCoord = latitudCoord.toString() + "," +  longitudCoord.toString();
											
											EnvioCoordCliente peticion = new EnvioCoordCliente(idSesion, ejemploCoord);											
											
											peticion.aplanar(dos);					
											
											//Pintamos nuestra coordenada enviada
											
										break;
										
										case Colision:
											
											Colision peticionColision = Colision.desaplanar(dis);					
											
											System.out.println("Has colisionado con la estela de: " + peticionColision.getUsuarioColision());
											System.out.println("Has obtenido una puntuaci�n de: " + peticionColision.getPuntuacion());

											//Reiniciamos los parametros del juego para el cliente
											
											juegoIniciado = false;
											listaDatosRivales = new ArrayList<DatosCliente>();
											
											break;
											
										default:
											break;
									}																										
									
								}
							}																		
						}
						
						else if (mensaje[0].equals("7") && (logeado) && peticionEnviada){
							
							bytes = new byte[4];
							n = -1;
							
							n = dis.read(bytes);
							
							if (n!=-1){
								
								RespuestaHistoricoPuntuaciones respuestaPeticion = RespuestaHistoricoPuntuaciones.desaplanar(dis);
								
								String[] puntuaciones = respuestaPeticion.getPuntuaciones().split("@");
								
								System.out.println("Puntos | UsuarioColision | fecha");
								
								for (String puntos : puntuaciones) {					        
									System.out.println(puntos);
							    }
															
							}							
								
						}
						
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("Petici�n realizada incorrectamente");
					}
				}
				else {
					
					peticionEnviada=false;
					
				}																			
			}
	
		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
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
						
			if(!usuarios[i].equals(miUsuario)) {
				
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
			
			for (String usuario : usuarios) {
				if(!EncontrarUsuarioLista(usuarios, usuario)) {
					EliminarRival(usuario);
				}
			}
			
			
		}
		
		//PINTAMOS COORDENADAS EN EL MAPA EN LA APP DE ANDROID las coordenadas de los usuarios que me llegan 
		}
		catch(IndexOutOfBoundsException e) {
								
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
