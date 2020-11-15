package base

/**
 * A thread to recover teams or matches stored to disk.
 */
@CompileStatic
class DataRecoveryThread extends Thread {

    private final boolean isTeam

    DataRecoveryThread(boolean isTeam) {
        this.isTeam = isTeam
    }

    @Override
    void run() {
        if (isTeam) {
            Main.openTeams.addAll(Lib.generateTeams(Main.currentSession.eventDir + 'teams.csv'))
            System.out.println('RECOVERING TEAMS')
            Main.addToOpenTeams(Lib.recoverTeams(Main.currentSession.eventDir))
            System.out.println(Main.openTeams.get(1).toReadableString())
            System.out.println('TEAMS RECOVERED')
        } else {
            System.out.println('RECOVERING MATCHES')
            Main.addToOpenMatches(Lib.recoverMatches(Main.currentSession.eventDir))
            System.out.println(Main.openMatches.get(1).toReadableString())
            System.out.println('MATCHES RECOVERED')
        }
        this.interrupt()
    }

}
