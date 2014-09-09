package packetlogger.core;

import java.io.IOException;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;


public abstract class Packet_Extend extends Packet {

	@PacketLoggerCopy
	public void overridden_readPacketData(PacketBuffer thing) throws IOException
	{
		PacketLogger.logPacket( (Packet)this );
		this.readPacketData(thing);
	}
	
}
