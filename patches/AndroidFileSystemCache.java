--- src/com/nutiteq/cache/AndroidFileSystemCache.java	2009-08-07 17:09:42.000000000 +0200
+++ src/com/nutiteq/cache/AndroidFileSystemCache.java	2010-12-07 17:59:22.000000000 +0100
@@ -56,13 +56,17 @@
   }
 
   private void deleteFilesFromFileSystem(final List<String> deletedFiles) {
-    for (final String file : deletedFiles) {
-      final File deleted = new File(cacheDir, file);
-      Log.debug("Deleting " + deleted.getAbsolutePath());
-      if (!deleted.delete()) {
-        Log.debug("No success");
-      }
-    }
+    new Thread(new Runnable() {
+        public void run() {
+            for (final String file : deletedFiles) {
+                final File deleted = new File(cacheDir, file);
+                Log.debug("Deleting " + deleted.getAbsolutePath());
+                if (!deleted.delete()) {
+                    Log.debug("No success");
+                }
+            }
+        }
+    }).start();
   }
 
   private String normalizeKey(final String cacheKey) {
