package hu.u_szeged.inf.aramis.adapter;

import android.app.ProgressDialog;
import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressBarHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressBarHandler.class);
    protected Context context;
    private ProgressDialog progressDialog;

    public ProgressBarHandler(Context context) {
        this.context = context;
    }

    public void start() {
        LOGGER.info("Starting progress dialog");
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Processing...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait.");
        progressDialog.show();
    }

    public void stop() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
