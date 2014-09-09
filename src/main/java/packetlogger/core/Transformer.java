package packetlogger.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.MessageDeserializer;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.fml.relauncher.FMLRelaunchLog;

public class Transformer implements IClassTransformer
{
	public static Transformer instance;

	public Transformer() {
		instance = this;
	}

	private void log(String str)
	{
		FMLRelaunchLog.log( "PacketLogger", Level.INFO, str );
	}

	@Override
	public byte[] transform(String className, String transformedName, byte[] bytes)
	{
		try
		{
			if ( "net.minecraft.network.Packet".equals( transformedName ) )
				bytes = transformPacket( bytes );

			if ( "net.minecraft.util.MessageDeserializer".equals( transformedName ) )
				bytes = transformMessagDeserializer( bytes );

			return bytes;
		}
		catch (Throwable t)
		{
			throw new RuntimeException( t );
		}
	}

	private byte[] transformPacket(byte[] bytes) throws IOException
	{
		log( "Found Packet" );

		log( MessageDeserializer.class.getName());
		
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader( bytes );
		classReader.accept( classNode, 0 );

		applyTemplate( classNode, "/packetlogger/core/Packet_Extend.class" );

		ClassWriter writer = new ClassWriter( ClassWriter.COMPUTE_MAXS );
		classNode.accept( writer );
		return writer.toByteArray();
	}

	private byte[] transformMessagDeserializer(byte[] bytes) throws IOException
	{
		log( "Found MessageDeserializer" );

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader( bytes );
		classReader.accept( classNode, 0 );

		MethodNode readpacketData = findMethod( classNode, MethodDesc.decode );
		if ( readpacketData != null )
		{
			Iterator<AbstractInsnNode> i = readpacketData.instructions.iterator();
			while (i.hasNext())
			{
				AbstractInsnNode node = i.next();
				if ( node instanceof MethodInsnNode )
				{
					MethodInsnNode in = (MethodInsnNode) node;
					if ( MethodDesc.readPacketData.isMethod( in ) )
					{
						in.name = "overridden_readPacketData";
						log( "MessageDeserializer - Calling new Method." );		
					}
				}
			}

			ClassWriter writer = new ClassWriter( ClassWriter.COMPUTE_MAXS );
			classNode.accept( writer );
			return writer.toByteArray();
		}

		return bytes;
	}

	private void applyTemplate(ClassNode classNode, String string) throws IOException
	{
		ClassNode srcNode = new ClassNode();
		InputStream is = getClass().getResourceAsStream( string );
		ClassReader srcReader = new ClassReader( is );
		srcReader.accept( srcNode, 0 );

		for (MethodNode mn : srcNode.methods)
		{
			if ( hasAnnotation( mn.visibleAnnotations, PacketLoggerCopy.class ) )
			{
				log( "Found " + mn.name );
				handleMethod( classNode, srcNode.name, mn );
			}
		}
	}

	private void handleMethod(ClassNode classNode, String from, MethodNode mn)
	{
		Iterator<AbstractInsnNode> i = mn.instructions.iterator();
		while (i.hasNext())
		{
			processNode( i.next(), from, classNode.name );
		}

		for (MethodNode tmn : classNode.methods)
		{
			if ( tmn.name.equals( mn.name ) && tmn.desc.equals( mn.desc ) )
			{
				log( "Found " + tmn.name + " : Appending" );

				AbstractInsnNode finalReturn = mn.instructions.getLast();
				while (!isReturn( finalReturn.getOpcode() ))
				{
					mn.instructions.remove( finalReturn );
					finalReturn = mn.instructions.getLast();
				}
				mn.instructions.remove( finalReturn );

				tmn.instructions.insert( mn.instructions );
				return;
			}
		}

		log( "No Such Method " + mn.name + " found on " + classNode.name + " : Adding" );
		classNode.methods.add( mn );
	}

	private boolean hasAnnotation(List<AnnotationNode> anns, Class<?> anno)
	{
		if ( anns == null )
			return false;

		for (AnnotationNode ann : anns)
		{
			if ( ann.desc.equals( Type.getDescriptor( anno ) ) )
			{
				return true;
			}
		}

		return false;
	}

	private boolean isReturn(int opcode)
	{
		switch (opcode)
		{
		case Opcodes.ARETURN:
		case Opcodes.DRETURN:
		case Opcodes.FRETURN:
		case Opcodes.LRETURN:
		case Opcodes.IRETURN:
		case Opcodes.RETURN:
			return true;
		}
		return false;
	}

	private void processNode(AbstractInsnNode next, String from, String nePar)
	{
		if ( next instanceof FieldInsnNode )
		{
			FieldInsnNode min = (FieldInsnNode) next;
			if ( min.owner.equals( from ) )
			{
				min.owner = nePar;
			}
		}
		if ( next instanceof MethodInsnNode )
		{
			MethodInsnNode min = (MethodInsnNode) next;
			if ( min.owner.equals( from ) )
			{
				min.owner = nePar;
			}
		}
	}

	private MethodNode findMethod(ClassNode classNode, MethodDesc md)
	{
		for (MethodNode mn : classNode.methods)
		{
			if ( md.isMethod( mn ) )
				return mn;
		}

		return null;
	}

}
