package utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFSUtils {
	public void local_to_dfs(String local_file, String dfs_file) {
		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(URI.create(dfs_file), conf);
			Path src = new Path(local_file);
			Path dst = new Path(dfs_file);
			fs.copyFromLocalFile(src, dst);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void dfs_to_local(String dfs_file, String local_file) {
		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(URI.create(dfs_file), conf);
			Path src = new Path(local_file);
			Path dst = new Path(dfs_file);
			fs.copyToLocalFile(true, dst, src);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void clear_dfs_path(String dfs_path) {
		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(URI.create(dfs_path), conf);
			fs.delete(new Path(dfs_path), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public boolean clear_local_path(File local_path) {
        if (local_path.isFile() && local_path.exists()) {
            local_path.delete();
            return true;
        }
        if (local_path.isDirectory()) {
            String[] children = local_path.list();
            for (String child : children) {
                boolean success = clear_local_path(new File(local_path, child));
                if (!success) {
                    return false;
                }
            }
        }
        return local_path.delete();
    }

	public static String check_input_path(String path) {
		if (!path.contains("/")) {
			System.out.println("Error: Please set full input file path, such as '/home/user/input.txt'");
			System.exit(0);
		}
		return path.substring(0, path.lastIndexOf("/"));
	}

}
