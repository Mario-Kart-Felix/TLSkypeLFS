package audiotest.takeleap.com.audiotestv1;

/**
 * Created by TakeLeap05 on 28-03-2018.
 */

class CommandResultCustom {
    final String output;
    final boolean success;

    CommandResultCustom(boolean success, String output) {
        this.success = success;
        this.output = output;
    }

    static  CommandResultCustom getDummyFailureResponse() {
        return new CommandResultCustom(false, "");
    }

    static CommandResultCustom getOutputFromProcess(Process process) {
        String output = "";
        if(success(Integer.valueOf(process.exitValue()))) {
            output = UtilCustom.convertInputStreamToString(process.getInputStream());
        } else {
            output = UtilCustom.convertInputStreamToString(process.getErrorStream());
        }

        return new CommandResultCustom(success(Integer.valueOf(process.exitValue())), output);
    }

    static boolean success(Integer exitValue) {
        return exitValue != null && exitValue.intValue() == 0;
    }
}