import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.actions.users.UserAction;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.models.user.Profile;
import com.github.instagram4j.instagram4j.models.user.User;
import com.github.instagram4j.instagram4j.actions.users.UsersAction;
import com.github.instagram4j.instagram4j.actions.users.UsersAction;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.actions.users.UsersAction;
import com.github.instagram4j.instagram4j.models.user.User;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.lang.Math;

import com.github.instagram4j.instagram4j.IGClient.Builder.LoginHandler;
import com.github.instagram4j.instagram4j.requests.friendships.FriendshipsActionRequest;
import com.github.instagram4j.instagram4j.responses.IGPaginatedResponse;
import com.github.instagram4j.instagram4j.responses.feed.FeedUsersResponse;
import com.github.instagram4j.instagram4j.responses.users.UsersSearchResponse;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;

import com.github.instagram4j.instagram4j.requests.users.*;


public class IGCheck {
    private IGClient client;
    private final String userName;
    private final String password;

    private boolean getVerifiedStatus(Profile person) {
        return person.is_verified();
    }

    public void unfollow(List<Profile> people) throws Exception {
        Profile p = people.get(0);
        if (!getVerifiedStatus(p))
            unfollowUser(p);
        System.out.println(p.getFull_name() + " has been unfollowed");

        Thread.sleep(100000);
    }

    public void unfollowUser(Profile person) throws Exception {
        if (client != null) {
            client.sendRequest(new FriendshipsActionRequest(person.getPk(), FriendshipsActionRequest.FriendshipsAction.DESTROY))
                    .thenAccept(response -> {
                        if (response.getStatus().equals("ok"))
                            System.out.println("Unfollowed " + person.getFull_name());
                        else
                            System.out.println("Error: " + response.getMessage());
                    })
                    .exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
        }
        else {
            System.out.println("Client not authenticated. Unfollow aborted.");
        }
    }

    //will output all the people that User follows but don't receive a follower from
    public List<Profile> getNonFollowers(User person) throws Exception {

        List<Profile> difference = new ArrayList<>();

        List<Profile> followers = getfollowers(person);
        List<Profile> following = getFollowing(person);

        if(followers.equals(following)){
            System.out.println("Everyone follows you back!");
            return difference;
        }

        System.out.println(Math.abs(followers.size() - following.size()) + " people don't follow you back");

        difference = new ArrayList<>(following);
        difference.removeAll(followers);

        return difference;





    }

    public List<Profile> getfollowers(User person) throws Exception{

        List<Profile> users = new ArrayList<>();

        UserAction userAction = new UserAction(client, person);

        for (FeedUsersResponse followersResponse : userAction.followersFeed()){
            users.addAll(followersResponse.getUsers());
        }

        return users;
    }


    public void Print(List<Profile> profiles){
        for(Profile profile : profiles){
            String userName = profile.getUsername();
            String name = profile.getFull_name();
            System.out.println("Username: " + userName + " Full Name: " + name);
        }
    }

    public List<Profile> getFollowing(User person){
        List<Profile> users = new ArrayList<>();


        UserAction userAction = new UserAction(client, person);

        for(FeedUsersResponse followerResponse : userAction.followingFeed()){
            users.addAll(followerResponse.getUsers());
        }

        return users;
    }



    public String formatter(List<Profile> users){
        String rv = "";
        int i = 0;
        while(users.get(i) != null){
            rv += users.get(i) + "\n";
        }
        return rv;

    }





    private Callable<String> inputCode(){
        Scanner scanner = new Scanner(System.in);

        String in = "";
        Callable<String> inputCode = () -> {
            System.out.print("Please input code: ");
            return scanner.nextLine();

        };

        return inputCode;

    }




    public boolean loginSuccess(Callable<String> inputCode) {

        LoginHandler twoFactorHandler = (client, response) -> {
            // included utility to resolve two factor
            // may specify retries. default is 3
            return IGChallengeUtils.resolveTwoFactor(client, response, inputCode);
        };


    try {
        client = IGClient.builder()
                .username(userName)
                .password(password)
                .onTwoFactor(twoFactorHandler)
                .login();
        return true;
    }
    catch (IGLoginException e) {
        System.err.println("Error occured: " + e);
        return false;
    }


    }


    public IGCheck(String username, String passWord) throws IGLoginException {
        this.userName = username;
        this.password = passWord;
    }

    public User getCurrentUser() throws Exception{

        return new UsersInfoRequest(client.getSelfProfile().getPk()).execute(client).join().getUser();

    }

    /*
    public List<Profile> NonUserNonFollowing(Profile person){
        System.out.println(person.getFull_name());

        List <Profile> users = new ArrayList<>();



    }

     */

    public static void main(String[] args) throws Exception {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter username:");
        String username = input.nextLine();
        System.out.println("Enter password:");
        String password = input.nextLine();

        IGCheck n = new IGCheck(username, password);
        boolean loginResult = n.loginSuccess(n.inputCode());



        if(loginResult){
            System.out.println("Login successful");
            n.Print(n.getNonFollowers(n.getCurrentUser()));
            //n.Print(n.getNonFollowers(n.getCurrentUser()));
        }
        else{
            System.out.println("Login failed. Please check your credentials or 2FA.");
        }





        }



}