/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;

    public int checkedListsCount = 0;

    LinkedList<Integer> occurrences = new LinkedList<>();

    LinkedList<ThreadValidator> threads = new LinkedList<>();

    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */

    public LinkedList<Integer> checkHost(String ipaddress, int N) throws InterruptedException {

        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        int rango = skds.getRegisteredServersCount() / N;

        for(int i=0;i<skds.getRegisteredServersCount(); i+=rango) {
            ThreadValidator thread = new ThreadValidator(i, i + rango - 1, ipaddress);
            thread.start();
            threads.add(thread);
        }

        for(ThreadValidator thread : threads) {
            thread.join();
            occurrences.addAll(thread.blackListOccurrences);
            checkedListsCount += thread.getcheckedListsCount();
        }

        if (occurrences.size()>=BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
            System.out.print(occurrences);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists: {0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});

        return occurrences;
    }


    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());

}
