package edu.eci.arsw.blacklistvalidator;

import java.util.LinkedList;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;



public class ThreadValidator extends Thread {
    int a, b, checkedLists;
    String ipaddress;
    LinkedList<Integer> blackListOccurrences = new LinkedList<Integer>();

    private static final int BLACK_LIST_ALARM_COUNT=5;

    public ThreadValidator (int a, int b, String ipaddress){
        this.a = a;
        this.b = b;
        this.ipaddress = ipaddress;
        checkedLists=0;
    }


    public void run(){

        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        for(int i = a; i <= b; i++){
            checkedLists++;
            if (skds.isInBlackListServer(i, ipaddress)){
                blackListOccurrences.add(i);
            }
        }
    }

    public Integer getcheckedListsCount(){ return checkedLists; }

    public LinkedList<Integer> getBlackListOccurrences(){
        return blackListOccurrences;
    }
}