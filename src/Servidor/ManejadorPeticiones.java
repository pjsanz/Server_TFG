package Servidor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;


import Entidades.BBDD;
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


public class ManejadorPeticiones implements Runnable {
	
	private ArrayList<DatosCliente> listaDatosClientes;
	private ArrayList<String> listaClientes;
	private Socket s;
	private Connection conn; //Conexión a la BBDD
	
	public ManejadorPeticiones(ArrayList<DatosCliente> listaDatosClientes, ArrayList<String> listaUsuarios, Socket s, Connection conn) {
		
		this.listaDatosClientes = listaDatosClientes;
		this.listaClientes 		= listaUsuarios;
		this.s 			  		= s;
		this.conn 				= conn;
		
	}
	
	public void run() {
		
		try{
			
			BufferedInputStream bis = new BufferedInputStream(this.s.getInputStream());
			DataInputStream     dis = new DataInputStream(bis);
			
			BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
			DataOutputStream 	 dos = new DataOutputStream(bos);
					           
			while(true){
				
				byte [] bytes = new byte[4];
				int n  = -1;
				n = dis.read(bytes);
				
				if (n!=-1){
					
					TipoMensaje tipo = TipoMensaje.values()[LittleEndian.desempaquetar(bytes)];
					
					switch (tipo) {
					
						case Autenticacion:
							
							Autenticacion peticionAutenticacion = Autenticacion.desaplanar(dis);
							System.out.println(peticionAutenticacion);
							
							RespuestaAutenticacion mensajeRespuestaRegistro;														
							
							//Caso para usuarios no registrados, realizamos la comprobación accediendo a la BBDD 
							
							BBDD consulta = new BBDD(conn);
							
							//Si se trata de un string vacio no existe usuario
							
							if (consulta.existeUsuario(peticionAutenticacion.getUsuario()).equals("")){
								
								//Si no existe le añadimos a la BBDD
								
								consulta.insertarUsuario(peticionAutenticacion.getUsuario(), peticionAutenticacion.getContraseña());
																
								//Le añadimos a nuestra lista de clientes activos
								
								listaClientes.add(peticionAutenticacion.getUsuario());
								
								System.out.println("------------ LISTA USUARIOS ACTIVOS -------------");
								System.out.println(listaClientes);
								
								//Introducimos en nuestra estructura todos los datos de los clientes
								
								DatosCliente datos = new DatosCliente(peticionAutenticacion.getUsuario());
								datos.anadirSocket(s);
																															 																																						
								mensajeRespuestaRegistro = new RespuestaAutenticacion("Registro");
								mensajeRespuestaRegistro.generarIdSesion();
								
								datos.setSesion(mensajeRespuestaRegistro.getIdSesion());
								listaDatosClientes.add(datos);
								
								//En los datos tenemos guardados el nick, el idSesion, el socket 
							}
							
							else if(consulta.existeUsuario(peticionAutenticacion.getUsuario()).equals("error")) {
								mensajeRespuestaRegistro = new RespuestaAutenticacion("Error");
								//Enviamos mensaje de error
							}
		
							else  //Recibimos una contraseña y comprobamos si coincide con la de la BBDD
							{
								
								if(consulta.existeUsuario(peticionAutenticacion.getUsuario()).equals(peticionAutenticacion.getContraseña())) {
									
									
									//Comprobamos si el usuario ya ha iniciado sesion
									
									Boolean sesionIniciada = false;
									
									for (DatosCliente datos : listaDatosClientes) {
								        if (datos.getUsuario().equals(peticionAutenticacion.getUsuario())) {
								        	sesionIniciada = true;
								            break;
								        }								
								    }
									
									if(sesionIniciada) {
										
										mensajeRespuestaRegistro = new RespuestaAutenticacion("Duplicado");
										
									}
									else {
																											
										//Si coincide guardamos todos los datos del cliente
									
										DatosCliente datos = new DatosCliente(peticionAutenticacion.getUsuario());
										datos.anadirSocket(s);
										
										System.out.println("------------ LISTA USUARIOS ACTIVOS -------------");
										System.out.println(listaClientes);								
										
										mensajeRespuestaRegistro = new RespuestaAutenticacion("OK");									
										mensajeRespuestaRegistro.generarIdSesion();
										
										datos.setSesion(mensajeRespuestaRegistro.getIdSesion());
										listaDatosClientes.add(datos);
									}
								}
								else {
									
									//Si no coincide mandamos la respuesta KO
									
									mensajeRespuestaRegistro = new RespuestaAutenticacion("KO");
									
								}													
							}
																			
							mensajeRespuestaRegistro.aplanar(dos);
							
							break;
							
							
						case InicioPartida:
							
							InicioPartida peticionInicio = InicioPartida.desaplanar(dis);
							System.out.println(peticionInicio);
							
							//Añadimos las coordenadas a la lista
							AñadirCoordenadasUsuarioListaClientes(peticionInicio.getIdSesion(), peticionInicio.getCoordenadas());
							
							//Enviamos las coordenadas, siempre la ultima
							EnvioCoordenadasServidor(peticionInicio.getIdSesion(), peticionInicio.getCoordenadas());														     				        
																			    																																									
							break;
							
						case EnvioCoordCliente:	
							
							EnvioCoordCliente peticionCoordCliente = EnvioCoordCliente.desaplanar(dis);							
							System.out.println(peticionCoordCliente);						
													
							//Añadimos las coordenadas a la lista
							AñadirCoordenadasUsuarioListaClientes(peticionCoordCliente.getIdSesion(), peticionCoordCliente.getCoordenadas());
							
							//Recibimos coordenadas del cliente comprobamos si hay colision con alguna de la lista de clientes que tenemos
							//Compararemos con las coordenadas de hora igual o superior a cuando se inicio la partida
							
							String usuarioColision = ComprobacionColision(peticionCoordCliente.getIdSesion(), peticionCoordCliente.getCoordenadas());
							
							if(usuarioColision.equals("")){	
								//Enviamos las coordenadas siempre la ultima
								EnvioCoordenadasServidor(peticionCoordCliente.getIdSesion(), peticionCoordCliente.getCoordenadas());														     				        
							}
							else {
								
								//Obtenemos los datos de los dos usuarios, el que envia sus coord y con el que colisiona
								
								DatosCliente datosClienteUsuario = ObtenerDatosClientePorIdSesion(peticionCoordCliente.getIdSesion());
															
								//La puntuacion la definimos como el numero de coordenadas que se han enviado al servidor hasta el momento de la colision
								
								BBDD consulta2 = new BBDD(conn);	
								consulta2.insertarPuntuacion(datosClienteUsuario.getUsuario(), datosClienteUsuario.getCoordenadas().size(), usuarioColision);
								
								//Enviamos el mensaje de colision al usuario para informarle que ha chocado con la estela de un jugador
								
								Colision peticionColision = new Colision(datosClienteUsuario.getSesion(), usuarioColision, String.valueOf(datosClienteUsuario.getCoordenadas().size()));
								peticionColision.aplanar(dos);
								
								//Desactivamos los dos el de la colision y el que nos envia sus coordenadas
								DesactivarUsuario(datosClienteUsuario.getSesion());
							}
							
														
							break;												
						
						case HistoricoPuntuaciones:	
							
							HistoricoPuntuaciones peticionPuntuaciones = HistoricoPuntuaciones.desaplanar(dis);	
							System.out.println(peticionPuntuaciones);
							
							//Obtenemos el historico de puntuaciones de la BBDD
															
							
							//Buscamos dentro de la lista de clientes por el id_Sesion recibido para conseguir el usuario
							String usuarioPuntuaciones = "";
							
							for (DatosCliente datos : listaDatosClientes) {
						        if (datos.getSesion().equals(peticionPuntuaciones.getIdSesion())) {
						        	usuarioPuntuaciones = datos.getUsuario();
						            break;
						        }
						    }
							
							BBDD consultaPuntuaciones = new BBDD(conn);							
														
							RespuestaHistoricoPuntuaciones respuestaPuntuaciones = new RespuestaHistoricoPuntuaciones(consultaPuntuaciones.obtenerPuntuaciones(usuarioPuntuaciones));																					
							respuestaPuntuaciones.aplanar(dos);
																																				
							break;
							
						case CerrarSesion:	
																					
							CerrarSesion peticionCerrarSesion = CerrarSesion.desaplanar(dis);							
							System.out.println(peticionCerrarSesion);

							//Eliminamos al usuario de la lista de jugadores activos
														
							EliminarSesionUsuario(peticionCerrarSesion.getIdSesion());
							
																													
							break;
						default:
														
							break;
						
					}
		
				}else {
					bis.close();
					dis.close();
					this.s.close();
					break;
				}
			}
			
		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void EnvioCoordenadasServidor(String idSesion, String coordenadas) {
		
		
		//Enviamos los datos de todos los clientes activos y su ultima coordenada la ultima posicion en la lista
		
		//Mandamos en dos strigs Usuarios por un lado y coordenadas por otro
		//Los usuarios estaran delimitados por: |
		//Las coordenadas entre ellas por | y de los distintos usuarios por @
		
		//Enviamos todas, luego el cliente pasará de las suyas ya que hemos guardado mi nick y de ese hay que pasar.
		
		//La logica de las colisiones la hara el cliente cuando el servidor mande las coordenadas de todos			
		
		//No guardo en BBDD las coordenadas porque no quiero que sean datos que persistan van con la partida.
												
		//Mandamos el tipo de mensaje EnvioCoordServidor con el string de coordenadas
		
		
		String usuarios = "";
		String coordenadasUsuarios = "";
		
		ArrayList<Coordenadas> listaAux = null;
		Coordenadas coordAux = null;
		
		for (DatosCliente datos : listaDatosClientes) {
			//Si el usuario esta activo en el juego empezamos a rellenar los strings a enviar		
			
	        if (datos.getEstado().equals("Activo") ){
	        	
	        	usuarios = usuarios + datos.getUsuario() + "&";
	        	
	        	listaAux = datos.getCoordenadas();
	        	
	        	coordAux = listaAux.get(listaAux.size()-1); //Obtenemos la ultima coordenada introducida la ultima posicion
	        	
	        	coordenadasUsuarios = coordenadasUsuarios + coordAux.getLatitud() + "," + coordAux.getLongitud() + "@";
	        }				        
	    }
		
		try {
			
			DatosCliente datosCli = ObtenerDatosClientePorIdSesion(idSesion);
			
			Socket s = datosCli.getSocket();
			
			BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
			DataOutputStream 	 dos = new DataOutputStream(bos);

			EnvioCoordServidor respuesta = new EnvioCoordServidor(usuarios,coordenadasUsuarios);	
	    	respuesta.aplanar(dos);
		
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		
	}

	private void DesactivarUsuario(String idSesion) {
		
		DatosCliente datosInicio = null;
		Integer indice = 0;
		
		//Buscamos dentro de la lista de clientes por el id_Sesion recibido
		
		for (DatosCliente datos : listaDatosClientes) {
	        if (datos.getSesion().equals(idSesion)) {
	        	datosInicio = datos;
	            break;
	        }
	        //Calculamos el indice para saber en que posicion está a la hora de actualizar
	        indice++;
	    }
		
		//Si hemos encontrado la sesion iniciada 
		
		//Ponemos el estado a Inactivo y reiniciamos sus coordenadas
		
		if(!datosInicio.equals(null)) {
								
			datosInicio.setEstado("Inactivo"); 	
			datosInicio.setCoordenadas(new ArrayList<Coordenadas>());
			
			listaDatosClientes.set(indice, datosInicio);	
		}							
		
		
	}
	
	private void EliminarSesionUsuario(String idSesion) {
		
		DatosCliente datosInicio = null;
		int indice = 0;
		
		//Buscamos dentro de la lista de clientes por el id_Sesion recibido
		
		for (DatosCliente datos : listaDatosClientes) {
	        if (datos.getSesion().equals(idSesion)) {
	        	datosInicio = datos;
	            break;
	        }
	        //Calculamos el indice para saber en que posicion está a la hora de actualizar
	        indice++;
	    }
		
		//Si hemos encontrado la sesion iniciada 
		
		if(!datosInicio.equals(null)) {								
			listaDatosClientes.remove(indice);	
		}							
		
		
	}

	private void AñadirCoordenadasUsuarioListaClientes(String idSesion, String coordenadas) {
		DatosCliente datosInicio = null;
		int indice = 0;
		
		//Buscamos dentro de la lista de clientes por el id_Sesion recibido
		
		for (DatosCliente datos : listaDatosClientes) {
	        if (datos.getSesion().equals(idSesion)) {
	        	datosInicio = datos;
	        	//Obtenemos el indice para saber en que posicion está a la hora de actualizar
	        	indice = listaDatosClientes.indexOf(datos);
	            break;
	        }
	    }
		
		//Si hemos encontrado la sesion iniciada 
		
		if(!datosInicio.equals(null)) {
			//enviamos nuestras coordenadas y solicitamos las otras
				
			//Miramos si el usuario esta activo para ver si es un mensaje de inicio de partida
			//Si es asi pasamos el usuario activo, sino simplemente es una peticion de coordenadas
			
			if(datosInicio.getEstado().equals("Inactivo")) {				
				datosInicio.setEstado("Activo"); 
				datosInicio.setHora(LocalDateTime.now()); //Añadimos la hora, solo compararemos 
				//con las coordenadas superiores a esa hora ya que ahi es cuando comenzamos a jugar
				//El usuario ahora estará con estado activo (ha empezado la partida)
			}
			
			
			//guardamos las coordenadas
					
			datosInicio.añadirCoordenadas(coordenadas);

			//Actualizamos el registro dentro de la listaDatosClientes en la posicion de la variable indice
			
			listaDatosClientes.set(indice, datosInicio);
		
		}							

	}
	
	private String ComprobacionColision(String idSesion, String ultimasCoordenadasUsuario) {
		
		String usuarioColision = "";						
		
		//Realizamos la comparacion por si hubiera colision comparamos con todos menos con nosotros mismos
				
		for (DatosCliente datosCliente : listaDatosClientes) {
			if(!datosCliente.getSesion().equals(idSesion)) {
				if(datosCliente.buscarCoordenada(ultimasCoordenadasUsuario, ObtenerHoraInicioPartida(idSesion))){
					usuarioColision = datosCliente.getUsuario();
					break;
				}
			}
	    }
			
		//Si hay colision envio el mensaje de colision al servidor, sino vuelvo a mandar mis coordenadas actuales
		
		return usuarioColision;
		
	}
		
	private DatosCliente ObtenerDatosClientePorIdSesion(String idSesion) {
		
		DatosCliente datosCli = null;
		
		for (DatosCliente datos : listaDatosClientes) {
	        if (datos.getSesion().equals(idSesion)) {
	        	datosCli = datos;
	            break;
	        }
	    }
		
		return datosCli;
	}
	/*
	private DatosCliente ObtenerDatosClientePorUsuario(String usuario) {
		
		DatosCliente datosCli = null;
		
		for (DatosCliente datos : listaDatosClientes) {
	        if (datos.getUsuario().equals(usuario)) {
	        	datosCli = datos;
	            break;
	        }
	    }
		
		return datosCli;
	}
	*/
	private LocalDateTime ObtenerHoraInicioPartida(String idSesion) {
		
		for (DatosCliente datosCliente : listaDatosClientes) {
			if(datosCliente.getSesion().equals(idSesion)) {
				return datosCliente.getHora();
			}
	    }
		
		return null;
		
	}
}
