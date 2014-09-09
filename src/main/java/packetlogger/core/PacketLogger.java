package packetlogger.core;

import java.io.File;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraftforge.common.config.Configuration;

public class PacketLogger {

	private static final Logger log = LogManager.getLogger();
	static Configuration c = new Configuration( new File( "./packetlogger.cfg" ) );
	
	static HashSet<Class> dolog = new HashSet();
	static HashSet<Class> dontLog = new HashSet();
	
	static HashSet<String> defaultBlackList = new HashSet();
	
	static {

		defaultBlackList.add("net.minecraft.network.play.server.S00PacketKeepAlive");
		defaultBlackList.add("net.minecraft.network.play.client.C00PacketKeepAlive");
		defaultBlackList.add("net.minecraft.network.play.client.C03PacketPlayer");
		defaultBlackList.add("net.minecraft.network.play.client.C03PacketPlayer$C04PacketPlayerPosition");
		defaultBlackList.add("net.minecraft.network.play.client.C03PacketPlayer$C05PacketPlayerLook");
		defaultBlackList.add("net.minecraft.network.play.client.S14PacketEntity$S15PacketEntityRelMove");
		defaultBlackList.add("net.minecraft.network.play.server.S14PacketEntity$S15PacketEntityRelMove");
		defaultBlackList.add("net.minecraft.network.play.server.S14PacketEntity$S16PacketEntityLook");
		defaultBlackList.add("net.minecraft.network.play.client.S14PacketEntity$S17PacketEntityLookMove");
		defaultBlackList.add("net.minecraft.network.play.server.S14PacketEntity$S17PacketEntityLookMove");
		defaultBlackList.add("net.minecraft.network.play.client.S19PacketEntityHeadLook");
		defaultBlackList.add("net.minecraft.network.play.server.S19PacketEntityHeadLook");		
		defaultBlackList.add("net.minecraft.network.play.client.S12PacketEntityVelocity");
		defaultBlackList.add("net.minecraft.network.play.server.S12PacketEntityVelocity");
		defaultBlackList.add("net.minecraft.network.play.client.C0APacketAnimation");
		defaultBlackList.add("net.minecraft.network.play.server.S1CPacketEntityMetadata");
		defaultBlackList.add("net.minecraft.network.play.client.C07PacketPlayerDigging");	
		defaultBlackList.add("net.minecraft.network.play.server.S20PacketEntityProperties");		
		defaultBlackList.add("net.minecraft.network.play.client.C03PacketPlayer$C06PacketPlayerPosLook");
		
	};
	
	public static boolean logPacket( Class which )
	{
		if ( dolog.contains( which ) )
			return true;

		if ( dontLog.contains( which ) )
			return false;
		
		boolean result = c.get("packets2log", which.getName(), !defaultBlackList.contains(which.getName()) ).getBoolean();
		
		if ( result == true )
			dolog.add(which);
		else
			dontLog.add(which);
		
		if ( c.hasChanged() )
			c.save();
		
		return result;
	}
	
	public static void logPacket(Packet packet)
	{
		if ( ! logPacket( packet.getClass() ) )
			return;
		
		if ( packet instanceof S35PacketUpdateTileEntity )
		{
			S35PacketUpdateTileEntity p = (S35PacketUpdateTileEntity)packet;
			
			NBTTagCompound tag = p.func_148857_g();
			
			if ( tag == null )
				log.info( "S35PacketUpdateTileEntity: @"+p.func_148856_c()+","+p.func_148855_d()+","+p.func_148854_e()+" containing NULL" );
			else
				log.info( "S35PacketUpdateTileEntity: @"+p.func_148856_c()+","+p.func_148855_d()+","+p.func_148854_e()+" containing "+tag.toString() );
		}
		else if ( packet instanceof S3FPacketCustomPayload )
		{
			S3FPacketCustomPayload p = (S3FPacketCustomPayload)packet;
			log.info( "S3FPacketCustomPayload: "+p.func_149169_c());
		}
		else if ( packet instanceof C17PacketCustomPayload )
		{
			C17PacketCustomPayload p = (C17PacketCustomPayload)packet;
			log.info( "C17PacketCustomPayload: "+p.func_149559_c());			
		}
		else
		{
			try
			{
				String out = packet.serialize();

				if ( out == null )
					log.info( packet.getClass().getName());
				else
					log.info( packet.getClass().getName()+": "+out);
			}
			catch(Throwable t)
			{
				log.info( packet.getClass().getName()+": failed to serialize");
			}
		}
	}
}
