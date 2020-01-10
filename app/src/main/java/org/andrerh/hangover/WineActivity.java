//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.andrerh.hangover;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

public class WineActivity extends Activity {
    private final String LOGTAG = "wine";
    private PointerIcon current_cursor;
    protected WineWindow desktop_window;
    protected WineWindow message_window;
    private ProgressDialog progress_dialog;
    private HashMap<Integer, WineWindow> win_map = new HashMap();

    public WineActivity() {
    }

    private final void copyAssetFile(String var1) {
        File var3 = new File(this.getFilesDir(), var1);

        label49:
        {
            boolean var10001;
            label48:
            {
                InputStream var4;
                FileOutputStream var5;
                byte[] var6;
                try {
                    Log.i("wine", "extracting " + var3);
                    var3.getParentFile().mkdirs();
                    var3.delete();
                    if (!var3.createNewFile()) {
                        break label48;
                    }

                    var4 = this.getAssets().open(var1);
                    var5 = new FileOutputStream(var3);
                    var6 = new byte[65536];
                } catch (IOException var11) {
                    var10001 = false;
                    break label49;
                }

                while (true) {
                    int var2;
                    try {
                        var2 = var4.read(var6);
                    } catch (IOException var9) {
                        var10001 = false;
                        break label49;
                    }

                    if (var2 <= 0) {
                        try {
                            var5.close();
                            if (this.isFileExecutable(var1)) {
                                var3.setExecutable(true, true);
                                return;
                            }

                            return;
                        } catch (IOException var8) {
                            var10001 = false;
                            break label49;
                        }
                    }

                    try {
                        var5.write(var6, 0, var2);
                    } catch (IOException var10) {
                        var10001 = false;
                        break label49;
                    }
                }
            }

            Log.i("wine", "Failed to create file " + var3);
            return;
        }

        Log.i("wine", "Failed to copy asset file to " + var3);
        var3.delete();
    }

    private final void copyAssetFiles() {
        String var1 = (String) this.readMapFromAssetFile("sums.sum").get("files.sum");
        if (var1 != null) {
            SharedPreferences var2 = PreferenceManager.getDefaultSharedPreferences(this);
            if (!var2.getString("files.sum", "").equals(var1)) {
                var2.edit().putString("files.sum", var1).apply();
                HashMap var8 = this.readMapFromDiskFile("files.sum");
                HashMap var3 = this.readMapFromAssetFile("files.sum");
                ArrayList var6 = new ArrayList();
                var6.add("files.sum");
                Iterator var10 = var3.entrySet().iterator();

                while (var10.hasNext()) {
                    Entry var4 = (Entry) var10.next();
                    String var5 = (String) var4.getKey();
                    if (!((String) var4.getValue()).equals(var8.remove(var5))) {
                        var6.add(var5);
                    }
                }

                this.createProgressDialog(var6.size(), "Extracting files...");
                Iterator var9 = var8.keySet().iterator();

                while (var9.hasNext()) {
                    this.deleteAssetFile((String) var9.next());
                }

                Iterator var7 = var6.iterator();

                while (var7.hasNext()) {
                    this.copyAssetFile((String) var7.next());
                    this.runOnUiThread(new Runnable() {
                        public void run() {
                            WineActivity.this.progress_dialog.incrementProgressBy(1);
                        }
                    });
                }
            }
        }

    }

    private void createProgressDialog(final int var1, final String var2) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (WineActivity.this.progress_dialog != null) {
                    WineActivity.this.progress_dialog.dismiss();
                }

                WineActivity.this.progress_dialog = new ProgressDialog(WineActivity.this);
                ProgressDialog var2x = WineActivity.this.progress_dialog;
                byte var1x;
                if (var1 > 0) {
                    var1x = 1;
                } else {
                    var1x = 0;
                }

                var2x.setProgressStyle(var1x);
                WineActivity.this.progress_dialog.setTitle("Hangover");
                WineActivity.this.progress_dialog.setMessage(var2);
                WineActivity.this.progress_dialog.setCancelable(false);
                WineActivity.this.progress_dialog.setMax(var1);
                WineActivity.this.progress_dialog.show();
            }
        });
    }

    private final void deleteAssetFile(String var1) {
        File var2 = new File(this.getFilesDir(), var1);
        Log.i("wine", "deleting " + var2);
        var2.delete();
    }

    @TargetApi(21)
    private String[] get_supported_abis() {
        return VERSION.SDK_INT >= 21 ? Build.SUPPORTED_ABIS : new String[]{Build.CPU_ABI};
    }

    private String get_wine_abi() {
        String[] var3 = this.get_supported_abis();
        int var2 = var3.length;

        for (int var1 = 0; var1 < var2; ++var1) {
            String var4 = var3[var1];
            if ((new File(this.getFilesDir(), var4 + "/bin/wineserver")).canExecute()) {
                return var4;
            }
        }

        Log.e("wine", "could not find a supported ABI");
        return null;
    }

    private final boolean isFileExecutable(String var1) {
        return !var1.equals("files.sum") && !var1.startsWith("share/");
    }

    private final boolean isFileWanted(String var1) {
        if (!var1.equals("files.sum") && !var1.startsWith("prefix/") && !var1.startsWith("share/")) {
            String[] var4 = this.get_supported_abis();
            int var3 = var4.length;

            for (int var2 = 0; var2 < var3; ++var2) {
                String var5 = var4[var2];
                if (var1.startsWith(var5 + "/system/")) {
                    return false;
                }

                if (var1.startsWith(var5 + "/")) {
                    return true;
                }
            }

            if (!var1.startsWith("x86/")) {
                return false;
            }
        }

        return true;
    }

    private void loadWine(String var1) {
        this.copyAssetFiles();
        String var3 = this.get_wine_abi();
        File var2 = new File(this.getFilesDir(), var3 + "/bin");
        File var4 = new File(this.getFilesDir(), var3 + "/lib");
        File var11 = new File(var4, "wine");
        File var5 = new File(this.getFilesDir(), "prefix");
        File var7 = new File(var2, "wine");
        String var8 = Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry() + ".UTF-8";
        HashMap var6 = new HashMap();
        var6.put("WINELOADER", var7.toString());
        var6.put("WINEPREFIX", var5.toString());
        var6.put("WINEDLLPATH", var11.toString());
        var6.put("LD_LIBRARY_PATH", var4.toString() + ":" + this.getApplicationInfo().nativeLibraryDir);
        var6.put("LC_ALL", var8);
        var6.put("LANG", var8);
        var6.put("PATH", var2.toString() + ":" + System.getenv("PATH"));
        String var10 = var1;
        if (var1 == null) {
            if ((new File(var5, "drive_c/winestart.cmd")).exists()) {
                var10 = "c:\\winestart.cmd";
            } else {
                var10 = "wineconsole.exe";
            }
        }

        var3 = this.readFileString(new File(var5, "winedebug"));
        var1 = var3;
        if (var3 == null) {
            var1 = this.readFileString(new File(this.getFilesDir(), "winedebug"));
        }

        if (var1 != null) {
            var11 = new File(this.getFilesDir(), "log");
            var6.put("WINEDEBUG", var1);
            var6.put("WINEDEBUGLOG", var11.toString());
            Log.i("wine", "logging to " + var11.toString());
            var11.delete();
        }

        this.createProgressDialog(0, "Setting up the Windows environment...");

        try {
            System.loadLibrary("wine");
        } catch (UnsatisfiedLinkError var9) {
            System.load(var4.toString() + "/libwine.so");
        }

        var5.mkdirs();
        this.runWine(var10, var6);
    }

    private final String readFileString(File var1) {
        try {
            String var3 = (new BufferedReader(new InputStreamReader(new FileInputStream(var1), "UTF-8"))).readLine();
            return var3;
        } catch (IOException var2) {
            return null;
        }
    }

    private final HashMap<String, String> readMapFromAssetFile(String var1) {
        try {
            HashMap var3 = this.readMapFromInputStream(this.getAssets().open(var1));
            return var3;
        } catch (IOException var2) {
            return new HashMap();
        }
    }

    private final HashMap<String, String> readMapFromDiskFile(String var1) {
        try {
            HashMap var3 = this.readMapFromInputStream(new FileInputStream(new File(this.getFilesDir(), var1)));
            return var3;
        } catch (IOException var2) {
            return new HashMap();
        }
    }

    private final HashMap<String, String> readMapFromInputStream(InputStream var1) {
        HashMap var2 = new HashMap();

        boolean var10001;
        BufferedReader var7;
        try {
            var7 = new BufferedReader(new InputStreamReader(var1, "UTF-8"));
        } catch (IOException var6) {
            var10001 = false;
            return var2;
        }

        while (true) {
            String var3;
            try {
                var3 = var7.readLine();
            } catch (IOException var5) {
                var10001 = false;
                break;
            }

            if (var3 == null) {
                break;
            }

            String[] var8 = var3.split("\\s+", 2);
            if (var8.length == 2 && this.isFileWanted(var8[1])) {
                var2.put(var8[1], var8[0]);
            }
        }

        return var2;
    }

    private final void runWine(String var1, HashMap<String, String> var2) {
        String[] var5 = new String[var2.size() * 2];
        int var3 = 0;

        int var4;
        Entry var7;
        for (Iterator var6 = var2.entrySet().iterator(); var6.hasNext(); var5[var4] = (String) var7.getValue()) {
            var7 = (Entry) var6.next();
            var4 = var3 + 1;
            var5[var3] = (String) var7.getKey();
            var3 = var4 + 1;
        }

        Log.e("wine", this.wine_init(new String[]{(String) var2.get("WINELOADER"), "explorer.exe", "/desktop=shell,,android", var1}, var5));
    }

    private native String wine_init(String[] var1, String[] var2);

    public void createDesktopWindow(final int var1) {
        this.runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            public void run() {
                WineActivity.this.create_desktop_window(var1);
            }
        });
    }

    public void createWindow(final int var1, final boolean var2, final int var3, final float var4, final int var5) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                WineActivity.this.create_window(var1, var2, var3, var4, var5);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void create_desktop_window(int var1) {
        Log.i("wine", String.format("create desktop view %08x", var1));
        this.setContentView(new TopView(this, var1));
        this.progress_dialog.dismiss();
        this.wine_config_changed(this.getResources().getConfiguration().densityDpi);
    }

    public void create_window(int var1, boolean var2, int var3, float var4, int var5) {
        WineWindow var7 = this.get_window(var1);
        WineWindow var6 = var7;
        if (var7 == null) {
            var7 = new WineWindow(var1, this.get_window(var3), var4);
            var7.create_window_groups();
            var6 = var7;
            if (var7.parent == this.desktop_window) {
                var7.create_whole_view();
                var6 = var7;
            }
        }

        if (var2) {
            var6.create_client_view();
        }

    }

    public void destroyWindow(final int var1) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                WineActivity.this.destroy_window(var1);
            }
        });
    }

    public void destroy_window(int var1) {
        WineWindow var2 = this.get_window(var1);
        if (var2 != null) {
            var2.destroy();
        }

    }

    protected WineWindow get_window(int var1) {
        return (WineWindow) this.win_map.get(var1);
    }

    public void onCreate(Bundle var1) {
        super.onCreate(var1);
        this.requestWindowFeature(1);
        (new Thread(new Runnable() {
            public void run() {
                WineActivity.this.loadWine((String) null);
            }
        })).start();
    }

    public void setCursor(final int var1, final int var2, final int var3, final int var4, final int var5, final int[] var6) {
        if (VERSION.SDK_INT >= 24) {
            this.runOnUiThread(new Runnable() {
                public void run() {
                    WineActivity.this.set_cursor(var1, var2, var3, var4, var5, var6);
                }
            });
        }
    }

    public void setParent(final int var1, final int var2, final float var3, final int var4) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                WineActivity.this.set_window_parent(var1, var2, var3, var4);
            }
        });
    }

    @TargetApi(24)
    public void set_cursor(int var1, int var2, int var3, int var4, int var5, int[] var6) {
        Log.i("wine", String.format("set_cursor id %d size %dx%d hotspot %dx%d", var1, var2, var3, var4, var5));
        if (var6 != null) {
            this.current_cursor = PointerIcon.create(Bitmap.createBitmap(var6, var2, var3, Config.ARGB_8888), (float) var4, (float) var5);
        } else {
            this.current_cursor = PointerIcon.getSystemIcon(this, var1);
        }
    }

    public void set_window_parent(int var1, int var2, float var3, int var4) {
        WineWindow var5 = this.get_window(var1);
        if (var5 != null) {
            var5.set_parent(this.get_window(var2), var3);
            if (var5.parent == this.desktop_window) {
                var5.create_whole_view();
                return;
            }
        }

    }

    public void windowPosChanged(int hwnd, int flags, int insert_after, int owner, int style, int window_left, int window_top, int window_right, int window_bottom, int client_left, int client_top, int client_right, int client_bottom, int visible_left, int visible_top, int visible_right, int visible_bottom) {
        final Rect window_rect = new Rect(window_left, window_top, window_right, window_bottom);
        final Rect client_rect = new Rect(client_left, client_top, client_right, client_bottom);
        final Rect visible_rect = new Rect(visible_left, visible_top, visible_right, visible_bottom);
        final int i = hwnd;
        final int i2 = flags;
        final int i3 = insert_after;
        final int i4 = owner;
        final int i5 = style;
        runOnUiThread(new Runnable() {
            public void run() {
                WineActivity.this.window_pos_changed(i, i2, i3, i4, i5, window_rect, client_rect, visible_rect);
            }
        });
    }

    public void window_pos_changed(int var1, int var2, int var3, int var4, int var5, Rect var6, Rect var7, Rect var8) {
        WineWindow var9 = this.get_window(var1);
        if (var9 != null) {
            var9.pos_changed(var2, var3, var4, var5, var6, var7, var8);
        }

    }

    public native void wine_config_changed(int var1);

    public native void wine_desktop_changed(int var1, int var2);

    public native boolean wine_keyboard_event(int var1, int var2, int var3, int var4);

    public native boolean wine_motion_event(int var1, int var2, int var3, int var4, int var5, int var6);

    public native void wine_surface_changed(int var1, Surface var2, boolean var3);

    protected class TopView extends ViewGroup {
        public TopView(Context var2, int var3) {
            super(var2);
            WineActivity.this.desktop_window = WineActivity.this.new WineWindow(var3, (WineWindow) null, 1.0F);
            this.addView(WineActivity.this.desktop_window.create_whole_view());
            WineActivity.this.desktop_window.client_group.bringToFront();
            WineActivity.this.message_window = WineActivity.this.new WineWindow(-3, (WineWindow) null, 1.0F);
            WineActivity.this.message_window.create_window_groups();
        }

        protected void onLayout(boolean var1, int var2, int var3, int var4, int var5) {
        }

        protected void onSizeChanged(int var1, int var2, int var3, int var4) {
            Log.i("wine", String.format("desktop size %dx%d", var1, var2));
            WineActivity.this.wine_desktop_changed(var1, var2);
        }
    }

    protected class WineView extends TextureView implements SurfaceTextureListener {
        private boolean is_client;
        private WineWindow window;

        public WineView(Context var2, WineWindow var3, boolean var4) {
            super(var2);
            this.window = var3;
            this.is_client = var4;
            this.setSurfaceTextureListener(this);
            this.setVisibility(0);
            this.setOpaque(false);
            this.setFocusable(true);
            this.setFocusableInTouchMode(true);
        }

        public boolean dispatchKeyEvent(KeyEvent var1) {
            Log.i("wine", String.format("view key event win %08x action %d keycode %d (%s)", this.window.hwnd, var1.getAction(), var1.getKeyCode(), KeyEvent.keyCodeToString(var1.getKeyCode())));
            boolean var3 = WineActivity.this.wine_keyboard_event(this.window.hwnd, var1.getAction(), var1.getKeyCode(), var1.getMetaState());
            boolean var2 = var3;
            if (!var3) {
                var2 = super.dispatchKeyEvent(var1);
            }

            return var2;
        }

        public WineWindow get_window() {
            return this.window;
        }

        public boolean onGenericMotionEvent(MotionEvent var1) {
            if (this.is_client) {
                return false;
            } else if (this.window.parent != null && this.window.parent != WineActivity.this.desktop_window) {
                return false;
            } else if ((var1.getSource() & 2) != 0) {
                int[] var2 = new int[2];
                this.window.get_event_pos(var1, var2);
                Log.i("wine", String.format("view motion event win %08x action %d pos %d,%d buttons %04x view %d,%d", this.window.hwnd, var1.getAction(), var2[0], var2[1], var1.getButtonState(), this.getLeft(), this.getTop()));
                return WineActivity.this.wine_motion_event(this.window.hwnd, var1.getAction(), var2[0], var2[1], var1.getButtonState(), (int) var1.getAxisValue(9));
            } else {
                return super.onGenericMotionEvent(var1);
            }
        }

        @TargetApi(24)
        public PointerIcon onResolvePointerIcon(MotionEvent var1, int var2) {
            return WineActivity.this.current_cursor;
        }

        public void onSurfaceTextureAvailable(SurfaceTexture var1, int var2, int var3) {
            int var4 = this.window.hwnd;
            String var5;
            if (this.is_client) {
                var5 = "client";
            } else {
                var5 = "whole";
            }

            Log.i("wine", String.format("onSurfaceTextureAvailable win %08x %dx%d %s", var4, var2, var3, var5));
            this.window.set_surface(var1, this.is_client);
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture var1) {
            int var2 = this.window.hwnd;
            String var3;
            if (this.is_client) {
                var3 = "client";
            } else {
                var3 = "whole";
            }

            Log.i("wine", String.format("onSurfaceTextureDestroyed win %08x %s", var2, var3));
            this.window.set_surface((SurfaceTexture) null, this.is_client);
            return false;
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture var1, int var2, int var3) {
            int var4 = this.window.hwnd;
            String var5;
            if (this.is_client) {
                var5 = "client";
            } else {
                var5 = "whole";
            }

            Log.i("wine", String.format("onSurfaceTextureSizeChanged win %08x %dx%d %s", var4, var2, var3, var5));
            this.window.set_surface(var1, this.is_client);
        }

        public void onSurfaceTextureUpdated(SurfaceTexture var1) {
        }

        public boolean onTouchEvent(MotionEvent var1) {
            if (!this.is_client && (this.window.parent == null || this.window.parent == WineActivity.this.desktop_window)) {
                int[] var2 = new int[2];
                this.window.get_event_pos(var1, var2);
                Log.i("wine", String.format("view touch event win %08x action %d pos %d,%d buttons %04x view %d,%d", this.window.hwnd, var1.getAction(), var2[0], var2[1], var1.getButtonState(), this.getLeft(), this.getTop()));
                return WineActivity.this.wine_motion_event(this.window.hwnd, var1.getAction(), var2[0], var2[1], var1.getButtonState(), 0);
            } else {
                return false;
            }
        }
    }

    protected class WineWindow {
        protected static final int HWND_MESSAGE = -3;
        protected static final int SWP_NOZORDER = 4;
        protected static final int WS_VISIBLE = 268435456;
        protected ArrayList<WineWindow> children;
        protected WineWindowGroup client_group;
        protected Rect client_rect;
        protected Surface client_surface;
        protected SurfaceTexture client_surftex;
        protected int hwnd;
        protected int owner;
        protected WineWindow parent;
        protected float scale;
        protected int style;
        protected boolean visible;
        protected Rect visible_rect;
        protected WineWindowGroup window_group;
        protected Surface window_surface;
        protected SurfaceTexture window_surftex;

        public WineWindow(int var2, WineWindow var3, float var4) {
            Log.i("wine", String.format("create hwnd %08x", var2));
            this.hwnd = var2;
            this.owner = 0;
            this.style = 0;
            this.visible = false;
            Rect var5 = new Rect(0, 0, 0, 0);
            this.client_rect = var5;
            this.visible_rect = var5;
            this.parent = var3;
            this.scale = var4;
            this.children = new ArrayList();
            WineActivity.this.win_map.put(var2, this);
            if (var3 != null) {
                var3.children.add(this);
            }

        }

        private void update_surface(boolean var1) {
            if (var1) {
                Log.i("wine", String.format("set client surface hwnd %08x %s", this.hwnd, this.client_surface));
                if (this.client_surface != null) {
                    WineActivity.this.wine_surface_changed(this.hwnd, this.client_surface, true);
                }
            } else {
                Log.i("wine", String.format("set window surface hwnd %08x %s", this.hwnd, this.window_surface));
                if (this.window_surface != null) {
                    WineActivity.this.wine_surface_changed(this.hwnd, this.window_surface, false);
                    return;
                }
            }

        }

        protected void add_view_to_parent() {
            int var2 = this.parent.client_group.getChildCount() - 1;
            int var1 = var2;
            if (var2 >= 0) {
                var1 = var2;
                if (this.parent.client_group.getChildAt(var2) == this.parent.client_group.get_content_view()) {
                    var1 = var2 - 1;
                }
            }

            byte var3 = 0;
            var2 = var1;

            int var5;
            for (var1 = var3; var1 < this.parent.children.size() && var2 >= 0; var2 = var5) {
                WineWindow var4 = (WineWindow) this.parent.children.get(var1);
                if (var4 == this) {
                    break;
                }

                if (!var4.visible) {
                    var5 = var2;
                } else {
                    var5 = var2;
                    if (var4 == ((WineWindowGroup) this.parent.client_group.getChildAt(var2)).get_window()) {
                        var5 = var2 - 1;
                    }
                }

                ++var1;
            }

            this.parent.client_group.addView(this.window_group, var2 + 1);
        }

        public void create_client_view() {
            if (this.client_group == null) {
                this.create_window_groups();
            }

            Log.i("wine", String.format("creating client view %08x %s", this.hwnd, this.client_rect));
            this.client_group.create_view(true).layout(0, 0, this.client_rect.right - this.client_rect.left, this.client_rect.bottom - this.client_rect.top);
        }

        public View create_whole_view() {
            if (this.window_group == null) {
                this.create_window_groups();
            }

            this.window_group.create_view(false).layout(0, 0, Math.round((float) (this.visible_rect.right - this.visible_rect.left) * this.scale), Math.round((float) (this.visible_rect.bottom - this.visible_rect.top) * this.scale));
            this.window_group.set_scale(this.scale);
            return this.window_group;
        }

        public WineWindowGroup create_window_groups() {
            if (this.client_group != null) {
                return this.client_group;
            } else {
                this.window_group = WineActivity.this.new WineWindowGroup(this);
                this.client_group = WineActivity.this.new WineWindowGroup(this);
                this.window_group.addView(this.client_group);
                this.client_group.set_layout(this.client_rect.left - this.visible_rect.left, this.client_rect.top - this.visible_rect.top, this.client_rect.right - this.visible_rect.left, this.client_rect.bottom - this.visible_rect.top);
                if (this.parent != null) {
                    this.parent.create_window_groups();
                    if (this.visible) {
                        this.add_view_to_parent();
                    }

                    this.window_group.set_layout(this.visible_rect.left, this.visible_rect.top, this.visible_rect.right, this.visible_rect.bottom);
                }

                return this.client_group;
            }
        }

        public void destroy() {
            Log.i("wine", String.format("destroy hwnd %08x", this.hwnd));
            this.visible = false;
            WineActivity.this.win_map.remove(this);
            if (this.parent != null) {
                this.parent.children.remove(this);
            }

            this.destroy_window_groups();
        }

        public void destroy_window_groups() {
            if (this.window_group != null) {
                if (this.parent != null && this.parent.client_group != null) {
                    this.remove_view_from_parent();
                }

                this.window_group.destroy_view();
            }

            if (this.client_group != null) {
                this.client_group.destroy_view();
            }

            this.window_group = null;
            this.client_group = null;
        }

        public void get_event_pos(MotionEvent var1, int[] var2) {
            var2[0] = Math.round(var1.getX() * this.scale + (float) this.window_group.getLeft());
            var2[1] = Math.round(var1.getY() * this.scale + (float) this.window_group.getTop());
        }

        public int get_hwnd() {
            return this.hwnd;
        }

        public void pos_changed(int var1, int var2, int var3, int var4, Rect var5, Rect var6, Rect var7) {
            boolean var9 = this.visible;
            this.visible_rect = var7;
            this.client_rect = var6;
            this.style = var4;
            this.owner = var3;
            boolean var8;
            if ((var4 & 268435456) != 0) {
                var8 = true;
            } else {
                var8 = false;
            }

            this.visible = var8;
            Log.i("wine", String.format("pos changed hwnd %08x after %08x owner %08x style %08x win %s client %s visible %s flags %08x", this.hwnd, var2, var3, var4, var5, var6, var7, var1));
            if ((var1 & 4) == 0 && this.parent != null) {
                this.set_zorder(WineActivity.this.get_window(var2));
            }

            if (this.window_group != null) {
                this.window_group.set_layout(var7.left, var7.top, var7.right, var7.bottom);
                if (this.parent != null) {
                    if (!var9 && (var4 & 268435456) != 0) {
                        this.add_view_to_parent();
                    } else if (var9 && (var4 & 268435456) == 0) {
                        this.remove_view_from_parent();
                    } else if (this.visible && (var1 & 4) == 0) {
                        this.sync_views_zorder();
                    }
                }
            }

            if (this.client_group != null) {
                this.client_group.set_layout(var6.left - var7.left, var6.top - var7.top, var6.right - var7.left, var6.bottom - var7.top);
            }

        }

        protected void remove_view_from_parent() {
            this.parent.client_group.removeView(this.window_group);
        }

        public void set_parent(WineWindow var1, float var2) {
            Log.i("wine", String.format("set parent hwnd %08x parent %08x -> %08x", this.hwnd, this.parent.hwnd, var1.hwnd));
            this.scale = var2;
            if (this.window_group != null) {
                if (this.visible) {
                    this.remove_view_from_parent();
                }

                var1.create_window_groups();
                this.window_group.set_layout(this.visible_rect.left, this.visible_rect.top, this.visible_rect.right, this.visible_rect.bottom);
            }

            this.parent.children.remove(this);
            this.parent = var1;
            this.parent.children.add(this);
            if (this.visible && this.window_group != null) {
                this.add_view_to_parent();
            }

        }

        public void set_surface(SurfaceTexture var1, boolean var2) {
            if (var2) {
                if (var1 == null) {
                    this.client_surface = null;
                } else if (var1 != this.client_surftex) {
                    this.client_surftex = var1;
                    this.client_surface = new Surface(var1);
                }
            } else if (var1 == null) {
                this.window_surface = null;
            } else if (var1 != this.window_surftex) {
                this.window_surftex = var1;
                this.window_surface = new Surface(var1);
            }

            this.update_surface(var2);
        }

        protected void set_zorder(WineWindow var1) {
            int var2 = -1;
            this.parent.children.remove(this);
            if (var1 != null) {
                var2 = this.parent.children.indexOf(var1);
            }

            this.parent.children.add(var2 + 1, this);
        }

        protected void sync_views_zorder() {
            int var1 = this.parent.children.size() - 1;

            int var3;
            WineWindow var4;
            for (int var2 = 0; var1 >= 0; var2 = var3) {
                var4 = (WineWindow) this.parent.children.get(var1);
                if (!var4.visible) {
                    var3 = var2;
                } else {
                    View var5 = this.parent.client_group.getChildAt(var2);
                    var3 = var2;
                    if (var5 != this.parent.client_group.get_content_view()) {
                        if (var4 != ((WineWindowGroup) var5).get_window()) {
                            break;
                        }

                        var3 = var2 + 1;
                    }
                }

                --var1;
            }

            for (; var1 >= 0; --var1) {
                var4 = (WineWindow) this.parent.children.get(var1);
                if (var4.visible) {
                    var4.window_group.bringToFront();
                }
            }

        }
    }

    protected class WineWindowGroup extends ViewGroup {
        private WineView content_view;
        private WineWindow win;

        WineWindowGroup(WineWindow var2) {
            super(WineActivity.this);
            this.win = var2;
            this.setVisibility(0);
        }

        public WineView create_view(boolean var1) {
            if (this.content_view != null) {
                return this.content_view;
            } else {
                this.content_view = WineActivity.this.new WineView(WineActivity.this, this.win, var1);
                this.addView(this.content_view);
                if (!var1) {
                    this.content_view.setFocusable(true);
                    this.content_view.setFocusableInTouchMode(true);
                }

                return this.content_view;
            }
        }

        public void destroy_view() {
            if (this.content_view != null) {
                this.removeView(this.content_view);
                this.content_view = null;
            }
        }

        public WineView get_content_view() {
            return this.content_view;
        }

        public WineWindow get_window() {
            return this.win;
        }

        protected void onLayout(boolean var1, int var2, int var3, int var4, int var5) {
            if (this.content_view != null) {
                this.content_view.layout(0, 0, var4 - var2, var5 - var3);
            }

        }

        public void set_layout(int var1, int var2, int var3, int var4) {
            int var5 = (int) ((float) var1 * this.win.scale);
            int var6 = (int) ((float) var2 * this.win.scale);
            var2 = (int) ((float) var3 * this.win.scale);
            var3 = (int) ((float) var4 * this.win.scale);
            var1 = var2;
            if (var2 <= var5 + 1) {
                var1 = var5 + 2;
            }

            var2 = var3;
            if (var3 <= var6 + 1) {
                var2 = var6 + 2;
            }

            this.layout(var5, var6, var1, var2);
        }

        public void set_scale(float var1) {
            if (this.content_view != null) {
                this.content_view.setPivotX(0.0F);
                this.content_view.setPivotY(0.0F);
                this.content_view.setScaleX(var1);
                this.content_view.setScaleY(var1);
            }
        }
    }
}
