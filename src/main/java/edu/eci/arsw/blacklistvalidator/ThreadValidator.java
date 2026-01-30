package edu.eci.arsw.blacklistvalidator;

import java.util.LinkedList;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;



public class ThreadValidator extends Thread {
    int a, b, checkedLists;
    String ipaddress;
    LinkedList<Integer> blackListOccurrences = new LinkedList<Integer>();

    private static final int BLACK_LIST_ALARM_COUNT=5;

    private HostBlackListsValidator validator;

    public ThreadValidator (int a, int b, String ipaddress, HostBlackListsValidator validator) {
        this.a = a;
        this.b = b;
        this.ipaddress = ipaddress;
        checkedLists=0;
        this.validator = validator;
    }



    public void run() {
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();

        for (int i = a; i < b && validator.getGlobalOccurrences() < BLACK_LIST_ALARM_COUNT; i++) {
            checkedLists++;
            if (skds.isInBlackListServer(i, ipaddress)) {
                blackListOccurrences.add(i);
                validator.addOccurrence();
            }
        }
    }

    public Integer getcheckedListsCount(){ return checkedLists; }

    public LinkedList<Integer> getBlackListOccurrences(){
        return blackListOccurrences;
    }

    public int getCheckedListsCount() {
        return checkedLists;
    }
}