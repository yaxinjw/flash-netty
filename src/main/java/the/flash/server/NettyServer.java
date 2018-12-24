package the.flash.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import the.flash.codec.PacketDecoder;
import the.flash.codec.PacketEncoder;
import the.flash.codec.Spliter;
import the.flash.server.handler.FirstServerHandler;
import the.flash.server.handler.LoginRequestHandler;
import the.flash.server.handler.MessageRequestHandler;

import java.util.Date;

public class NettyServer {

    private static final int PORT = 8000;

    public static void main(String[] args) {
        NioEventLoopGroup boosGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        final ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(boosGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) {
                    	// 固定长度拆包
//                        ch.pipeline().addLast(new LineBasedFrameDecoder(2048));
	                    // 行拆包
//                        ch.pipeline().addLast(new FixedLengthFrameDecoder(28));
	                    // 指定分隔符拆包
//	                    ch.pipeline().addLast(new DelimiterBasedFrameDecoder(2048, Unpooled.copiedBuffer("\n".getBytes())));

	                    // 业务handler
//                        ch.pipeline().addLast(new FirstServerHandler());

	                    // 拆包
	                    ch.pipeline().addLast(new Spliter());
	                    // 解码
	                    ch.pipeline().addLast(new PacketDecoder());
	                    // 处理登录请求
                        ch.pipeline().addLast(new LoginRequestHandler());
                        // 处理消息接受
                        ch.pipeline().addLast(new MessageRequestHandler());
                        // 给对端发送数据包编码
                        ch.pipeline().addLast(new PacketEncoder());
                    }
                });


        bind(serverBootstrap, PORT);
    }

    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println(new Date() + ": 端口[" + port + "]绑定成功!");
            } else {
                System.err.println("端口[" + port + "]绑定失败!");
            }
        });
    }
}
