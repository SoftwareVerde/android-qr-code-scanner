package com.softwareverde.qrcodescanner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.TextView;
import android.widget.Toast;

import com.softwareverde.json.Json;
import com.softwareverde.logging.Log;
import com.softwareverde.util.StringUtil;

public class ScanResultActivity extends AppCompatActivity {
    protected String _barcodeContent;

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);

        this.setContentView(R.layout.scan_result_activity_layout);

        final Intent intent = this.getIntent();
        _barcodeContent = intent.getStringExtra(MainActivity.BARCODE_DATA);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final boolean isJson = Json.isJson(_barcodeContent);
        final boolean isUrl = (! StringUtil.pregMatch("^([A-Za-z0-9\\-_]+):/?/?(.*)$", _barcodeContent.trim()).isEmpty());

        if (isJson) {
            final Json json = Json.parse(_barcodeContent);
            _barcodeContent = json.toFormattedString();
        }

        final View copyButton = this.findViewById(R.id.scan_result_copy_button);
        final TextView contentTypeTextView = ((TextView) this.findViewById(R.id.scan_result_type_textview));
        final TextView textView = ((TextView) this.findViewById(R.id.scan_result_textview));

        if (isJson) {
            contentTypeTextView.setText("ContentType: TEXT/JSON");

            textView.setTypeface(Typeface.MONOSPACE);
            textView.setHorizontallyScrolling(true);
            textView.setMovementMethod(new ScrollingMovementMethod());
        }
        else if (isUrl) {
            contentTypeTextView.setText("ContentType: TEXT/URL");
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F);
            textView.setHorizontallyScrolling(true);
            textView.setMovementMethod(new ScrollingMovementMethod());

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    try {
                        final Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(_barcodeContent.trim()));
                        ScanResultActivity.this.startActivity(intent);
                    }
                    catch (final Exception exception) {
                        exception.printStackTrace();
                    }

                    ScanResultActivity.this.finish();
                }
            });
        }
        else {
            contentTypeTextView.setText("ContentType: TEXT");
        }

        textView.setText(_barcodeContent);

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final ClipData clip = ClipData.newPlainText("QR Code", _barcodeContent);

                final ClipboardManager clipboard = (ClipboardManager) ScanResultActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(ScanResultActivity.this, "Copied to clipboard.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
