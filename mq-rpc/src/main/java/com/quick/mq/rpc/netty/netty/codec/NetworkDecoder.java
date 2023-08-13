package com.quick.mq.rpc.netty.netty.codec;

import com.quick.mq.common.exchange.NettyMessage;
import com.quick.mq.common.t_enum.VersionEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.charset.StandardCharsets;

/**
 * Netty Codec 适配器，使用定长头+字节数组作为自定义编码协议（防止TCP沾包问题）
 * <pre>
 *      0     1     2     3      4        5        6        7         8   9   10   11    12    13    14   15
 *   +-----+-----+-----+-----+-------+--------+-------+-------------+---+---+----+----+-----+-----+-----+----+
 *   |   magic   number      |version|compress| codec | messageType |   messageId     |     dataLength       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         data                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4Byte magic number（魔法数）  1Byte version（版本）     1Byte compress（压缩类型）  1Byte codec（序列化类型）
 * 1Byte messageType（消息类型） 4Byte messageId（消息Id） 4Byte dataLength（消息长度）data（object类型数据）
 * </pre>
 * <p>
 * {@link LengthFieldBasedFrameDecoder} 是一个基于长度域的解码器，用于解决TCP沾包问题（不用我们手动处理）。
 * </p>
 */
public class NetworkDecoder extends LengthFieldBasedFrameDecoder {

  public static final int HEAD_LENGTH = 16;
  public static final int MAX_FRAME_LENGTH = 1024 * 1024 * 10;

  public NetworkDecoder() {
    this(MAX_FRAME_LENGTH, 12, 4, 0, 0);
  }

  /**
   * @param maxFrameLength 最大数据帧长度。如果接收的数据超过此长度，数据将被丢弃，并抛出 TooLongFrameException。
   * @param lengthFieldOffset 长度域偏移量。
   * @param lengthFieldLength 长度域字节数，即用几个字节来表示数据长度（此属性的值是数据长度）。
   * @param lengthAdjustment 数据长度修正（数据长度 + lengthAdjustment = 数据总长度）。长度域指定的长度可以是header+body的整个长度，也可以只是body的长度。如果表示header+body的整个长度，那么需要修正数据长度。
   * @param initialBytesToStrip 跳过的字节数。如果你需要接收header+body的所有数据，此值就是0；如果你只想接收body数据，那么需要跳过header所占用的字节数
   */
  public NetworkDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
      int initialBytesToStrip) {
    super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
  }

  @Override
  protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    Object decode = super.decode(ctx, in);
    if (decode instanceof ByteBuf) {
      ByteBuf data = (ByteBuf) decode;
      if (data.readableBytes() < HEAD_LENGTH) {
        return decode;
      }
      return decodeBody(data);
    }

    return decode;
  }

  private Object decodeBody(ByteBuf in) {
    // 读取header
    byte[] header = new byte[HEAD_LENGTH];
    in.readBytes(header);
    // check magic num
    byte[] magicNum = Bytes.copyOf(header, 4);
    // check version
    if (header[4] != VersionEnum.V1.getVersion()) {
      throw new IllegalStateException(
          "Versions are not consistent, expect version: [" + 1 + "], actual version: [" + header[4] + "]");
    }

    byte compressType = header[5];
    byte serializeType = header[6];
    byte messageType = header[7];
    int messageId = Bytes.bytes2int(header, 8);
    int dataLength = Bytes.bytes2int(header, 12);
    // 解析数据
    byte[] dataBytes = new byte[dataLength];
    in.readBytes(dataBytes);
    String data = new java.lang.String(dataBytes ,StandardCharsets.UTF_8);
    NettyMessage message = new NettyMessage(data, compressType, serializeType, messageType);
    message.setMsgId(messageId);

    return message;
  }
}
