package crazypants.enderio;

import com.enderio.core.common.TileEntityBase;
import com.enderio.core.common.vecmath.Vector4f;

import crazypants.enderio.config.Config;
import crazypants.enderio.machine.ranged.MarkerParticle;
import info.loenwind.autosave.Reader;
import info.loenwind.autosave.Writer;
import info.loenwind.autosave.annotations.Store.StoreFor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityEio extends TileEntityBase {

  protected boolean doingOtherNbt = false;

  @Override
  public final SPacketUpdateTileEntity getUpdatePacket() {
    NBTTagCompound root = createClientUpdateNBT();
    
    return new SPacketUpdateTileEntity(getPos(), 1, root);
  }
  
  @Override
  public NBTTagCompound getUpdateTag() {    
    return createClientUpdateNBT();
  }

  protected NBTTagCompound createClientUpdateNBT() {
    NBTTagCompound root = new NBTTagCompound();
    try {
      doingOtherNbt = true;
      super.writeToNBT(root);
    } finally {
      doingOtherNbt = false;
    }
    Writer.write(StoreFor.CLIENT, root, this);
    return root;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public final void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
    NBTTagCompound root = pkt.getNbtCompound();
    Reader.read(StoreFor.CLIENT, root, this);
    try {
      doingOtherNbt = true;
      super.readFromNBT(root);
    } finally {
      doingOtherNbt = false;
    }
    onAfterDataPacket();
    if (Config.debugUpdatePackets) {
      Minecraft.getMinecraft().effectRenderer.addEffect(new MarkerParticle(worldObj, pos, new Vector4f(1, 182f / 255f, 0, 0.4f)));
    }
  }

  protected void onAfterDataPacket() {
  }

  @Override
  protected void writeCustomNBT(NBTTagCompound root) {
    if (!doingOtherNbt) {
      Writer.write(StoreFor.SAVE, root, this);
    }
  }

  @Override
  protected void readCustomNBT(NBTTagCompound root) {
    if (!doingOtherNbt) {
      Reader.read(StoreFor.SAVE, root, this);
    }
  }

  public void readContentsFromNBT(NBTTagCompound nbtRoot) {
    Reader.read(StoreFor.ITEM, nbtRoot, this);
  }

  public void writeContentsToNBT(NBTTagCompound nbtRoot) {
    Writer.write(StoreFor.ITEM, nbtRoot, this);
  }

}
