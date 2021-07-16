package Entidades;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BBDD {
	
	private Connection conn ;
	
	public BBDD(Connection conn){		
		this.conn = conn;		
	}
	
	public Boolean insertarUsuario(String usuario, String password) {
		
		//Realizamos el insert del nuevo usuario en la BBDD si todo ha ido bien devolvera true, sino false
		
		try {
			  
			Statement st = conn.createStatement();
	           
            String INSERT = "INSERT INTO [UsuariosAPP].[dbo].[usuarios] "
            				+ "VALUES ('"+ usuario +"','"+ password +"',getdate())";

            st.execute(INSERT);         
 
        	return true;
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
	}
	
	public String existeUsuario(String usuario) {
		
		//Comprobamos si existe el usuario, si es asi nos devolvera su clave y podremos hacer la comparativa, 
		//si no existe devolvera un string vacio y por tanto querra decir que no existe
		
		try {
			  
			Statement st = conn.createStatement();
			
			String SELECT = "SELECT * FROM [UsuariosApp].[dbo].[usuarios]";
            
            ResultSet res = st.executeQuery(SELECT); 
            
            String usuarioBBDD = "";
            
            while(res.next()) {
            	
            	usuarioBBDD = res.getString("usuario"); 
            	
            	if(usuario.equals(usuarioBBDD)) {
            		return res.getString("password"); 
            	}

            }
					
        	return "";
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "-1";
        }
	}			
	
	public Integer obtenerIdUsuario(String usuario) {
		//Comprobamos si existe el usuario, si es as� nos devolvera su clave y podremos hacer la comparativa, 
		//si no existe devolvera un string vacio y por tanto querra decir que no existe
		
		try {
			  
			Statement st = conn.createStatement();
			
			String SELECT = "SELECT id_usuario FROM [UsuariosAPP].[dbo].[usuarios] WHERE usuario = '"+ usuario +"'";
            
            ResultSet res = st.executeQuery(SELECT); 
            		            
            while(res.next()) {
            			            	
        		return Integer.parseInt (res.getString("id_usuario")); 

            }
					
        	return 0;
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
	}
	
	public Boolean insertarPuntuacion(String usuario, int puntuacion, String usuarioColision) {
		
		//Realizamos el insert del nuevo usuario en la BBDD si todo ha ido bien devolvera true, sino false
		
		try {
			  
			Statement st = conn.createStatement();
	           
            String INSERT = "INSERT INTO [UsuariosAPP].[dbo].[puntuaciones] "
            				+ "VALUES ('"+ obtenerIdUsuario(usuario) +"','"+ puntuacion +"','"+ obtenerIdUsuario(usuarioColision) +"',getdate())";

            st.execute(INSERT);         
 
        	return true;
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
	}
	
	public String obtenerPuntuaciones(String usuario) {
		
		//Comprobamos si existe el usuario, si es as� nos devolvera su clave y podremos hacer la comparativa, 
		//si no existe devolvera un string vacio y por tanto querra decir que no existe
		
		try {
			  
			Statement st = conn.createStatement();
			
			String SELECT = "SELECT pun.puntuacion, pun.fecha_puntuacion, usu.usuario AS usuarioColision"
					+ "FROM [UsuariosApp].[dbo].[puntuaciones] pun"
					+ "JOIN [UsuariosApp].[dbo].[usuarios] usu ON usu.id_usuario = pun.id_usuarioColision"
					+ "WHERE id_usuario ='"+ obtenerIdUsuario(usuario) +"'";
            
            ResultSet res = st.executeQuery(SELECT); 
            
            String puntuaciones = "";
            
            while(res.next()) {
            	
            	puntuaciones = puntuaciones + res.getString("puntuacion") + "|" + res.getString("usuarioColision")
            				 + "|" + res.getString("fecha_puntuacion") + "@"; 

            }
					
        	return puntuaciones;
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "error";
        }
	}			
	
}
