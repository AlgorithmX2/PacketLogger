package packetlogger.core;

import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * A nicer way to handle Dev vs Runtime naming schemes
 */
enum MethodDesc
{
	readPacketData("a", "func_148837_a", "readPacketData", "(Let;)V", "(Lnet/minecraft/network/PacketBuffer;)V"),

	decode("a", "decode", "decode", "(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Lj ava/util/List;)V",
			"(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Ljava/util/List;)V");

	String notch, srg, dev;
	String desc0, desc1;

	private MethodDesc(String notch, String srg, String dev, String desc0, String desc1) {
		this.notch = notch;
		this.srg = srg;
		this.dev = dev;
		this.desc0 = desc0;
		this.desc1 = desc1;
	}

	boolean isMethod(MethodInsnNode mn)
	{
		return (mn.name.equals( notch ) || mn.name.equals( srg ) || mn.name.equals( dev )) && (mn.desc.equals( desc0 ) || mn.desc.equals( desc1 ));
	}

	boolean isMethod(MethodNode mn)
	{
		return (mn.name.equals( notch ) || mn.name.equals( srg ) || mn.name.equals( dev )) && (mn.desc.equals( desc0 ) || mn.desc.equals( desc1 ));
	}
}