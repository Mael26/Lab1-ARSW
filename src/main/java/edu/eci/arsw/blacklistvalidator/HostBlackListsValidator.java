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

    public volatile int globalOccurrences = 0;

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
        // 1. Inicializar variables locales para que no se acumulen datos de ejecuciones previas
        LinkedList<Integer> localOccurrences = new LinkedList<>();
        LinkedList<ThreadValidator> threadList = new LinkedList<>();
        int totalCheckedLists = 0;

        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        int totalServers = skds.getRegisteredServersCount();

        // 2. Cálculo preciso de rangos
        int rangoBase = totalServers / N;
        int residuo = totalServers % N;

        int inicio = 0;
        for (int i = 0; i < N; i++) {
            // El último hilo absorbe el residuo si la división no es exacta
            int fin = inicio + rangoBase + (i == N - 1 ? residuo : 0);

            // Se crea el hilo con el rango [inicio, fin)
            ThreadValidator thread = new ThreadValidator(inicio, fin, ipaddress, this);
            threadList.add(thread);
            thread.start();

            inicio = fin;
        }

        // 3. Esperar a que TODOS terminen (Join)
        for (ThreadValidator thread : threadList) {
            thread.join();
            localOccurrences.addAll(thread.getBlackListOccurrences());
            totalCheckedLists += thread.getCheckedListsCount();
        }

        // 4. Lógica de reporte (Alarm Count = 5)
        if (localOccurrences.size() >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists: {0} of {1}", new Object[]{totalCheckedLists, totalServers});

        return localOccurrences;
    }

    public synchronized void addOccurrence() {
        this.globalOccurrences++;
    }

    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());

    public Integer getGlobalOccurrences() {
        return globalOccurrences;
    }
}