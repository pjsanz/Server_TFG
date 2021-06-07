package Entidades;

public enum TipoMensaje {
	
	Autenticacion, // 0
	RespuestaAutenticacion, // 1
	InicioPartida, //2
	EnvioCoordServidor, // 3
	EnvioCoordCliente, //4
	Colision, //5
	HistoricoPuntuaciones, //6
	RespuestaHistoricoPuntaciones, //7
	CerrarSesion // 8
}
