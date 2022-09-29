package mixins;

import dev.hoot.api.events.PacketSent;
import eventbus.Events;
import net.runelite.api.mixins.Inject;
import net.runelite.api.mixins.MethodHook;
import net.runelite.api.mixins.Mixin;
import net.runelite.api.mixins.Shadow;
import net.runelite.api.packets.PacketBufferNode;
import net.runelite.rs.api.RSClient;
import net.runelite.rs.api.RSPacketBufferNode;
import net.runelite.rs.api.RSPacketWriter;

@Mixin(RSPacketWriter.class)
public abstract class RSPacketWriterMixin implements RSPacketWriter
{
	@Shadow("client")
	private static RSClient client;

	@Inject
	@Override
	public void queuePacket(PacketBufferNode packetBufferNode)
	{
		sendPacket((RSPacketBufferNode) packetBufferNode);
	}

	@Inject
	@MethodHook("addNode")
	public void addNode(RSPacketBufferNode packet)
	{
		client.getCallbacks().post(Events.PACKET_SENT, new PacketSent(packet));
	}
}
