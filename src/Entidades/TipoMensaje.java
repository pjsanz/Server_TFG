package Entidades;

public enum TipoMensaje {
	
	Autenticacion, // 0
	RespuestaAutenticacion, // 1
	InicioPartida, //2
	EnvioCoordServidor, // 3
	EnvioCoordCliente, //4
	Colision, //5
	Puntuacion, //6
	HistoricoPuntuaciones, //7
	RespuestaHistoricoPuntaciones, //8
	CerrarSesion // 9
}
