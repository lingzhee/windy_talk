package com.windy.talk.socket;


import com.alibaba.fastjson.JSON;
import com.windy.talk.vo.ChatVo;
import com.windy.talk.vo.Message;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
@ServerEndpoint("/chatSocket")
public class ChatSocket {

    private String username;
    private static List<Session> session = new ArrayList<Session>();
    private static Map<String, Session> map = new HashMap<String,Session>();
    private static List<String> names = new ArrayList<String>();
    @OnOpen
    public void open(Session session) throws UnsupportedEncodingException {
        String queryString = session.getQueryString();
        username=queryString.split("=")[1];
        System.out.println(username);
        username = URLDecoder.decode(username, "utf-8");
        this.session.add(session);
        this.names.add(username);
        this.map.put(this.username, session);
        String msg = "<div class='row'>欢迎"+this.username+"进入聊天室！！</div>";
        Message message = new Message();
        message.setCurrentUserName(this.username);
        message.setUserNames(this.names);
        message.setWelcome(msg);
        System.out.println(message.toJson());
        this.broadcast(this.session,message.toJson());
    }
    private void broadcast(List<Session> ss, String msg) {
        for(Session session:ss) {
            try {
                Session currentUser = this.map.get(this.username);

                session.getBasicRemote().sendText(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    @OnClose
    public void close(Session session) {
        this.session.remove(session);
        this.names.remove(this.username);
        this.map.remove(this.username);
        String msg = "<div class='row'>欢送"+this.username+"离开聊天室！！</div>";
        Message message = new Message();
        message.setCurrentUserName(this.username);
        message.setUserNames(this.names);
        message.setWelcome(msg);
        broadcast(this.session, message.toJson());
    }
    @OnMessage
    public void message(Session session,String msg)  {
        ChatVo chat = JSON.parseObject(msg, ChatVo.class);
        if (chat.getType()==0) {
            String msg2 = "<div class='row'>"+this.username+"说： "+chat.getMsg()+"</div>";//前台显示的话
            Message message = new Message();
            message.setCurrentUserName(this.username);
            message.setMessage(msg2);
            broadcast(this.session, message.toJson());
        } else {
            String msg2 = "<div class='row'>"+this.username+"对你说： "+chat.getMsg()+"</div>";//前台显示的话
            Session to = this.map.get(chat.getTo());
            Message message = new Message();
            message.setCurrentUserName(this.username);
            message.setMessage(msg2);
            try {
                to.getBasicRemote().sendText(message.toJson());

                Message message1 = new Message();
                //v-bind:class="[jack ? 'col-sm-offset-5' : '']"
                String msg3 = "<div class='row'>你对 "+chat.getTo()+"说： "+chat.getMsg()+"</div>";//前台显示的话
                message1.setMessage(msg3);
                message.setCurrentUserName(this.username);
                session.getBasicRemote().sendText(message1.toJson());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

