package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashMap<String,User> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashMap<>();
        this.customGroupCount = 1;
        this.messageId = 1;
    }

    public String createUser(String name, String mobile) throws Exception{
        if(userMobile.containsKey(mobile)){
            throw new Exception("User already exists");
        }
        User user=new User();
        user.setName(name);
        user.setMobile(mobile);
        userMobile.put(mobile,user);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        int n=users.size();
        Group group=new Group();
        if(n==2){
            group.setName(users.get(1).getName());
            group.setNumberOfParticipants(n);
        }
        else{
            group.setName("Group "+customGroupCount);
            customGroupCount++;
            group.setNumberOfParticipants(n);
        }
        groupUserMap.put(group,users);
        adminMap.put(group,users.get(0));
        return group;
    }

    public int createMessage(String content) {
        Message message=new Message();
        message.setId(messageId);
        messageId++;
        message.setContent(content);
        return message.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        int n=0;
        if(!(groupUserMap.containsKey(group))){
            throw new Exception("Group does not exist");
        }
        else if(!(groupUserMap.get(group).contains(sender))){
            throw new Exception("You are not allowed to send message");
        }
        else {
            List<Message> messages=new ArrayList<>();
            if(groupMessageMap.containsKey(group)) {
                messages = groupMessageMap.get(group);
            }
            messages.add(message);
            groupMessageMap.put(group,messages);
            n=messages.size();
            senderMap.put(message,sender);
        }
        return n;
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        if(!(groupUserMap.containsKey(group))){
            throw new Exception("Group does not exist");
        }
        else if(!(adminMap.get(group).equals(approver))){
            throw new Exception("Approver does not have rights");
        }
        else if(!(groupUserMap.get(group).contains(user))){
            throw new Exception("User is not a participant");
        }
        else{
            adminMap.put(group,user);
            return "SUCCESS";
        }
    }

    public int removeUser(User user) throws Exception{

        Group groupuser=new Group();
        for(Group group:groupUserMap.keySet()){
            List<User> userList=groupUserMap.get(group);
            for(User users:userList){
                if(users.equals(user)){
                    groupuser=group;
                    break;
                }
            }
        }
        if(groupuser.getName()==null){
            throw new Exception("User not found");
        }
        else if(adminMap.get(groupuser).equals(user)){
            throw new Exception("Cannot remove admin");
        }
        else {
            List<User> users=groupUserMap.get(groupuser);
            users.remove(user);
            List<Message> messages1=new ArrayList<>();
            for(Message message:senderMap.keySet()){
                if(senderMap.get(message).equals(user)){
                    List<Message> messages=groupMessageMap.get(groupuser);
                    messages.remove(message);
                    messages1.add(message);
                }
            }
            for(Message message:messages1){
                senderMap.remove(message);
            }

        }
        String mobile=user.getMobile();
        userMobile.remove(mobile);
        int n=0;
        List<User> usersingroup=groupUserMap.get(groupuser);
        List<Message> messageList=groupMessageMap.get(groupuser);
        n=usersingroup.size()+messageList.size()+senderMap.size();
        return n;
    }

    public String findMessage(Date start, Date end, int k) throws Exception{
        List<Message> messageList=new ArrayList<>();
        for(Message message:senderMap.keySet()){
            if(message.getTimestamp().after(start)&&message.getTimestamp().before(end)){
                messageList.add(message);
            }
        }
        messageList.sort((a, b) -> {
            return a.getId() - b.getId();
        });
        if(messageList.size()<k)throw new Exception("K is greater than the number of messages");
        return messageList.get(k-1).getContent();
    }
}
