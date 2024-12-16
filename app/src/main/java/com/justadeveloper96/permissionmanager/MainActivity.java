package com.justadeveloper96.permissionmanager;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Toast;

import com.justadeveloper96.permissionhelper.PermissionHelper;
import com.justadeveloper96.permissionhelper.PermissionHelper.PermissionsListener;
import com.justadeveloper96.permissionmanager.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by harshit on 10-03-2017.
 */


public class MainActivity extends AppCompatActivity{

    PermissionHelper permissionHelper;
    ObservableField<Integer> permissioncount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        permissioncount=new ObservableField<>(0);
        binding.setCount(permissioncount);

        final List<String> needed_permissions=new ArrayList<>();
        final List<PermissionModel> manifest_permissions=new ArrayList<>();

        /**
         * Get all app permissions from Manifest
         */
        manifest_permissions.addAll(getAllDeniedPermissions(this));

        final ArrayAdapter<PermissionModel> arrayAdapter=new ArrayAdapter<PermissionModel>(this,android.R.layout.simple_list_item_checked,manifest_permissions);
        binding.listview.setAdapter(arrayAdapter);
        binding.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(arrayAdapter.getItem(i).isSelected())
                {
                    arrayAdapter.getItem(i).setSelected(false);
                    ((CheckedTextView)view).setChecked(false);
                    permissioncount.set(permissioncount.get()-1);
                    return;
                }
                arrayAdapter.getItem(i).setSelected(true);
                ((CheckedTextView)view).setChecked(true);
                permissioncount.set(permissioncount.get()+1);
            }
        });


        /**
         * Initialize permission Manager and set Listener.
         */
        permissionHelper =new PermissionHelper(this).setListener(new PermissionsListener() {
            @Override
            public void onPermissionGranted(int request_code) {
                Toast.makeText(MainActivity.this,"Granted all",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRejectedManyTimes(@NonNull List<String> rejectedPerms, int request_code) {
                String s="";
                for(String perms:rejectedPerms)
                {
                    s+=","+ PermissionHelper.getNameFromPermission(perms);
                }
                Toast.makeText(MainActivity.this,"Rejected "+s.substring(1),Toast.LENGTH_SHORT).show();
            }
        });

        binding.get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                needed_permissions.clear();
                for (int i=0;i<arrayAdapter.getCount();i++)
                {
                    PermissionModel item=arrayAdapter.getItem(i);
                    if(item.isSelected())
                    {
                        needed_permissions.add(item.getValue());
                    }
                }
                permissionHelper.requestPermission(needed_permissions.toArray(new String[needed_permissions.size()]),100);
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        /**
         * pass the permission results to the PermissionManager for processing.
         */
        permissionHelper.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        permissionHelper.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static List<PermissionModel> getAllDeniedPermissions(Context ctx)
    {
        List<PermissionModel> temp=new ArrayList<>();
        try {
            PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_PERMISSIONS);
            if (info.requestedPermissions != null) {
                for (String p : info.requestedPermissions) {
                    if (ctx.checkSelfPermission(p)==PackageManager.PERMISSION_DENIED)
                        temp.add(new PermissionModel(p));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return temp;
    }
}
