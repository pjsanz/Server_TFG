package Servidor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;

import Entidades.Coordenadas;
import Entidades.DatosCliente;
import Entidades.LittleEndian;
import Entidades.TipoMensaje;

import Peticiones.Autenticacion;
import Peticiones.EnvioCoordServidor;
import Peticiones.InicioPartida;
import Peticiones.RespuestaAutenticacion;


public class ManejadorPeticiones implements Runnable {
	
	private Hashtable<String,String> tablaUsuarios;
	private ArrayList<DatosCliente> listaDatosClientes;
	private ArrayList<String> listaClientes;
	private Socket s;
	
	public ManejadorPeticiones(ArrayList<DatosCliente> listaDatosClientes, ArrayList<String> listaUsuarios, Socket s) {
		
		this.listaDatosClientes = listaDatosClientes;
		this.listaClientes 		= listaUsuarios;
		this.s 			  		= s;
		
		//Simulamos la BBDD
		this.tablaUsuarios = new Hashtable<String,String>();
		this.tablaUsuarios.put("Pablo", "1234");
		
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
							
							//Caso para usuarios no registrados
							
							if (!this.tablaUsuarios.containsKey(peticionAutenticacion.getUsuario())){
								
								this.tablaUsuarios.put(peticionAutenticacion.getUsuario(), peticionAutenticacion.getContraseña());
								
								listaClientes.add(peticionAutenticacion.getUsuario());
								
								DatosCliente datos = new DatosCliente(peticionAutenticacion.getUsuario());
								datos.anadirSocket(s);
																							
								/*
								Con BBDD SQLSERVER
								
								Connection conn = null;
								
								  try {
									  
							            String dbURL = "jdbc:sqlserver://DESKTOP-FVD51I9\\MSSQLSERVER:1433";
							            String user = "usuarioAPP";
							            String pass = "psswrd";
							            conn = DriverManager.getConnection(dbURL, user, pass);
							            
							            Statement st = conn.createStatement();
							           
							            String INSERT = "INSERT INTO [Usuarios].[dbo].[usuarios] VALUES ('" + peticion.getUsuario() +"','" + peticion.getContraseña() +"',getdate())";

							            st.execute(INSERT);
							            
							            String SELECT = "SELECT * FROM [Usuarios].[dbo].[usuarios]";
							            
							            ResultSet res = st.executeQuery(SELECT); 
							            
							            while(res.next()) {
							            	String nombre = res.getString("usuario"); 
							            	System.out.println(nombre);
							            }
							 
							        } catch (SQLException ex) {
							            ex.printStackTrace();
							        } finally {
							            try {
							                if (conn != null && !conn.isClosed()) {
							                    conn.close();
							                }
							            } catch (SQLException ex) {
							                ex.printStackTrace();
							            }
							        }
								
								*/
								 															
								System.out.println("------------ LISTA USUARIOS ACTIVOS -------------");
								System.out.println(listaClientes);
								
								System.out.println("------------ BASE DE DATOS USUARIOS -------------");
								System.out.println(this.tablaUsuarios);
								
								
								mensajeRespuestaRegistro = new RespuestaAutenticacion("OK");
								mensajeRespuestaRegistro.generarIdSesion();
								
								datos.setSesion(mensajeRespuestaRegistro.getIdSesion());
								listaDatosClientes.add(datos);
								//En los datos tenemos guardados el nick, el idSesion, el socket 
							}
		
							else  //Comprobamos si la contraseña es correcta ya que se trata de un nombre de usuario registrado
							{
								
								if(this.tablaUsuarios.get(peticionAutenticacion.getUsuario()).equals(peticionAutenticacion.getContraseña())) {
																		
									DatosCliente datos = new DatosCliente(peticionAutenticacion.getUsuario());
									datos.anadirSocket(s);
									
									System.out.println("------------ LISTA USUARIOS ACTIVOS -------------");
									System.out.println(listaClientes);
									
									System.out.println("------------ BASE DE DATOS USUARIOS -------------");
									System.out.println(this.tablaUsuarios);
									
									mensajeRespuestaRegistro = new RespuestaAutenticacion("OK");									
									mensajeRespuestaRegistro.generarIdSesion();
									
									datos.setSesion(mensajeRespuestaRegistro.getIdSesion());
									listaDatosClientes.add(datos);
									
								}
								else {
									
									mensajeRespuestaRegistro = new RespuestaAutenticacion("KO");
									
								}													
							}
																			
							mensajeRespuestaRegistro.aplanar(dos);
							
							break;
							
							
						case InicioPartida:
							
							InicioPartida peticionInicio = InicioPartida.desaplanar(dis);
							System.out.println(peticionInicio);
							
							
							
							DatosCliente datosInicio = null;
							Integer indice = 0;
							
							//Buscamos dentro de la lista de clientes por el id_Sesion recibido
							
							for (DatosCliente datos : listaDatosClientes) {
						        if (datos.getSesion().equals(peticionInicio.getIdSesion())) {
						        	datosInicio = datos;
						            break;
						        }
						        //Calculamos el indice para saber en que posicion está a la hora de actualizar
						        indice++;
						    }
							
							//Si hemos encontrado la sesion iniciada:
							
							if(!datosInicio.equals(null)) {
								//enviamos nuestras coordenadas y solicitamos las otras
																
								//El usuario ahora estará con estado activo (ha empezado la partida)
								//y guardamos sus primeras coordenadas
								
								datosInicio.setEstado("Activo"); 
								datosInicio.añadirCoordenadas(peticionInicio.getCoordenadas());
								
								//Actualizamos el registro dentro de la listaDatosClientes en la posicion de la variable indice
								
								listaDatosClientes.set(indice, datosInicio);
							
							}
							
																																	
							//Ahora enviamos los datos de todos los clientes activos y su ultima coordenada, la más reciente en fecha
							//Enviare coordenadas con usuario para saber quien es
							//Lo mando todo en un unico string con delimitadores para hacer luego un split. 
							//lo vamos añadiendo con el delimitador '||' para separar coordenadas de usuarios
							//para separar coordenadas del campo usuario utilizaremos el separador '@'														
							//No guardo en BBDD las coordenadas porque no quiero que sean datos que persistan.
										
							
							//Mandamos el tipo de mensaje EnvioCoordServidor con el string de coordenadas
							
							EnvioCoordServidor respuesta = new EnvioCoordServidor("Coordenadas");		
							respuesta.aplanar(dos);
							
							break;
							
						
						default:
							
							//Respuesta mensajeRespuestaERROR = new Respuesta("Comando no vÃ¡lido");
							//mensajeRespuestaERROR.aplanar(dos);
							
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
		}
		
	}
	
	
}
