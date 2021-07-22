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
import java.util.UUID;

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
	private Socket s;
	private Connection conn; //Conexion a la BBDD
	
	public ManejadorPeticiones(ArrayList<DatosCliente> listaDatosClientes, ArrayList<String> listaUsuarios, Socket s, Connection conn) {
		
		this.listaDatosClientes = listaDatosClientes;
		this.s = s;
		this.conn = conn;
		
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
							System.out.println("Peticion recibida");
							RespuestaAutenticacion respuestaAutenticacion = ValidacionesInicioSesion(dis);	
							respuestaAutenticacion.aplanar(dos);
							
							break;						
							
						case InicioPartida:
							
							InicioPartida peticionInicio = InicioPartida.desaplanar(dis);
							System.err.println(peticionInicio);
							
							//Insertamos las coordenadas a la lista
							InsertarCoordenadasUsuarioListaClientes(peticionInicio.getIdSesion(), peticionInicio.getCoordenadas(), "Inicio");
							
							//Enviamos las coordenadas, siempre la ultima
							EnvioCoordenadasServidor(peticionInicio.getIdSesion(), peticionInicio.getCoordenadas());														     				        
																			    																																									
							break;
							
						case EnvioCoordCliente:	
							
							EnvioCoordCliente peticionCoordCliente = EnvioCoordCliente.desaplanar(dis);							
							System.err.println(peticionCoordCliente);						
													
							//Insertamos las coordenadas a la lista
							InsertarCoordenadasUsuarioListaClientes(peticionCoordCliente.getIdSesion(), peticionCoordCliente.getCoordenadas(), "");
							
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
							System.err.println(peticionPuntuaciones);											
							
							String puntuaciones = ObtenerPuntuacionesBBDD(peticionPuntuaciones);
							
							RespuestaHistoricoPuntuaciones respuestaPuntuaciones = new RespuestaHistoricoPuntuaciones(puntuaciones);																					
							respuestaPuntuaciones.aplanar(dos);
																																				
							break;
							
						case CerrarSesion:	
																					
							CerrarSesion peticionCerrarSesion = CerrarSesion.desaplanar(dis);							
							System.err.println(peticionCerrarSesion);

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
	
	private RespuestaAutenticacion ValidacionesInicioSesion(DataInputStream dis) throws Exception {
		
		Autenticacion peticionAutenticacion = Autenticacion.desaplanar(dis);
		System.err.println(peticionAutenticacion);
		
		String idSesion = "";
		RespuestaAutenticacion mensajeRespuestaRegistro;														
		
		//Caso para usuarios no registrados, realizamos la comprobacion accediendo a la BBDD 
		
		BBDD consulta = new BBDD(conn);
		
		//Comprobamos en BBDD el usuario recibido. Si el usuario no existe la password sera "" y tendremos que crearle en BBDD
		
		String password = consulta.existeUsuario(peticionAutenticacion.getUsuario());
		
		if (password.equals("")){
			
			//Si no existe le insertamos a la BBDD y a la lista de clientes activos
			
			idSesion = InsertarUsuarioBBDD(peticionAutenticacion, consulta);
						
			//Generamos la respuesta
			
			
			mensajeRespuestaRegistro = new RespuestaAutenticacion("Registro");
			mensajeRespuestaRegistro.setIdSesion(idSesion);

		}
		
		else if(password.equals("-1")) {
			mensajeRespuestaRegistro = new RespuestaAutenticacion("Error");
			//Enviamos mensaje de error
		}

		else  //Recibimos una contrasena y comprobamos si coincide con la de la BBDD
		{
			
			if(password.equals(peticionAutenticacion.getPassword())) {
							
				//Comprobamos si el usuario ya ha iniciado sesion por si quiere entrar con sesion duplicada
								
				if(ComprobarUsuarioInicioSesion(peticionAutenticacion.getUsuario())) {
					
					mensajeRespuestaRegistro = new RespuestaAutenticacion("Duplicado");
					
				}
				else {
																						
					//Si coincide guardamos todos los datos del cliente
					
				    idSesion = GuardarDatosCliente(peticionAutenticacion);
					mensajeRespuestaRegistro = new RespuestaAutenticacion("OK");
					mensajeRespuestaRegistro.setIdSesion(idSesion);
				}
			}
			else {
				
				//Si no coincide mandamos la respuesta KO
				
				mensajeRespuestaRegistro = new RespuestaAutenticacion("KO");
				
			}													
		}
														
		System.out.println(mensajeRespuestaRegistro.getIdSesion());
		return mensajeRespuestaRegistro;
	}
	
	private String GenerarIdSesion() {
		
	    return UUID.randomUUID().toString();	 
	    
	} 
	
	private String InsertarUsuarioBBDD(Autenticacion peticionAutenticacion, BBDD consulta) {
		
		//insertamos a la BBDD
		
		consulta.insertarUsuario(peticionAutenticacion.getUsuario(), peticionAutenticacion.getPassword());											
		
		//Introducimos en nuestra estructura todos los datos de los clientes
		
		String idSesion = GenerarIdSesion();
		
		DatosCliente datos = new DatosCliente(peticionAutenticacion.getUsuario());
		datos.anadirSocket(s);
																									 																																								
		datos.setSesion(idSesion);
		listaDatosClientes.add(datos);
		
		//En los datos tenemos guardados el nick, el idSesion, el socket 
		
		return idSesion;
	}
	
	private String GuardarDatosCliente(Autenticacion peticionAutenticacion) {
		
		String idSesion = GenerarIdSesion();
		
		DatosCliente datos = new DatosCliente(peticionAutenticacion.getUsuario());
		datos.anadirSocket(s);
										
		datos.setSesion(idSesion);
		listaDatosClientes.add(datos);
		
		return idSesion;
		
	}
	
	private boolean ComprobarUsuarioInicioSesion(String usuario) {
		
		Boolean sesionIniciada = false;
		
		for (DatosCliente datos : listaDatosClientes) {
	        if (datos.getUsuario().equals(usuario)) {
	        	sesionIniciada = true;
	            break;
	        }								
	    }
		
		return sesionIniciada;
	}
	
	private String ObtenerUsuarioPorIdSesion(String idSesion) {
		
		String usuario = "";
		
		for (DatosCliente datos : listaDatosClientes) {
	        if (datos.getSesion().equals(idSesion)) {
	        	usuario = datos.getUsuario();
	            break;
	        }
	    }
		
		return usuario;
	}
	
	private void EnvioCoordenadasServidor(String idSesion, String coordenadas) {
		
		
		//Enviamos los datos de todos los clientes activos y su ultima coordenada la ultima posicion en la lista
		
		//Mandamos en dos strigs Usuarios por un lado y coordenadas por otro
		//Los usuarios estaran delimitados por: |
		//Las coordenadas entre ellas por | y de los distintos usuarios por @
		
		//Enviamos todas, luego el cliente pasar� de las suyas ya que hemos guardado mi nick y de ese hay que pasar.
		
		//La logica de las colisiones la hara el cliente cuando el servidor mande las coordenadas de todos			
		
		//No guardo en BBDD las coordenadas porque no quiero que sean datos que persistan van con la partida.
												
		//Mandamos el tipo de mensaje EnvioCoordServidor con el string de coordenadas
		
		
		String usuarios = "";
		String coordenadasUsuarios = "";
		
		ArrayList<Coordenadas> listaAux = null;
		Coordenadas coordAux = null;
		
		for (DatosCliente datos : listaDatosClientes) {
			//Si el usuario esta activo en el juego empezamos a rellenar los strings a enviar		
			
	        if (datos.getEstado().equals("Activo") && !datos.getSesion().equals(idSesion)){
	        	
	        	usuarios = usuarios + datos.getUsuario() + "&";
	        	
	        	listaAux = datos.getCoordenadas();
	        	
	        	coordAux = listaAux.get(listaAux.size()-1); //Obtenemos la ultima coordenada introducida la ultima posicion
	        	
	        	coordenadasUsuarios = coordenadasUsuarios + coordAux.getLatitud() + "," + coordAux.getLongitud() + "@";
	        }				        
	    }
		
		try {
			
			//DatosCliente datosCli = ObtenerDatosClientePorIdSesion(idSesion);
			
			//Socket s = datosCli.getSocket();
			
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
	        	indice = listaDatosClientes.indexOf(datos);
	        	datosInicio = datos;
	            break;
	        }

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
	        	indice = listaDatosClientes.indexOf(datos);
	        	datosInicio = datos;
	            break;
	        }
	    }
		
		//Si hemos encontrado la sesion iniciada 
		
		if(!datosInicio.equals(null)) {								
			listaDatosClientes.remove(indice);	
		}							
		
		
	}

	private void InsertarCoordenadasUsuarioListaClientes(String idSesion, String coordenadas, String llamada) {
		
		DatosCliente datosInicio = null;
		int indice = 0;
		
		//Buscamos dentro de la lista de clientes por el id_Sesion recibido
		
		for (DatosCliente datos : listaDatosClientes) {
	        if (datos.getSesion().equals(idSesion)) {
	        	datosInicio = datos;
	        	//Obtenemos el indice para saber en que posicion est� a la hora de actualizar
	        	indice = listaDatosClientes.indexOf(datos);
	            break;
	        }
	    }
		
		//Si hemos encontrado la sesion iniciada 
		
		if(!datosInicio.equals(null)) {
			//enviamos nuestras coordenadas y solicitamos las otras
				
			//Miramos si el usuario esta activo para ver si es un mensaje de inicio de partida
			//Si es asi pasamos el usuario activo, sino simplemente es una peticion de coordenadas
			
			if(llamada.equals("Inicio") && datosInicio.getEstado().equals("Inactivo")) {				
				datosInicio.setEstado("Activo"); 
				datosInicio.setHora(LocalDateTime.now()); //A�adimos la hora, solo compararemos 
				//con las coordenadas superiores a esa hora ya que ahi es cuando comenzamos a jugar
				//El usuario ahora estar� con estado activo (ha empezado la partida)
			}
			
			
			//guardamos las coordenadas
					
			datosInicio.insertarCoordenadas(coordenadas);

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

	private LocalDateTime ObtenerHoraInicioPartida(String idSesion) {
		
		for (DatosCliente datosCliente : listaDatosClientes) {
			if(datosCliente.getSesion().equals(idSesion)) {
				return datosCliente.getHora();
			}
	    }
		
		return null;
		
	}

	private String ObtenerPuntuacionesBBDD(HistoricoPuntuaciones peticionPuntuaciones) {
		
		//Obtenemos el historico de puntuaciones de la BBDD
		
		//Buscamos dentro de la lista de clientes por el id_Sesion recibido para conseguir el usuario
		String usuarioPuntuaciones = ObtenerUsuarioPorIdSesion(peticionPuntuaciones.getIdSesion());					
		
		BBDD consultaPuntuaciones = new BBDD(conn);							
									
		return consultaPuntuaciones.obtenerPuntuaciones(usuarioPuntuaciones);																					
	}
}
