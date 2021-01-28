package com.sty.ne.protocolbuffer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void buildPerson() {
        AddressProto.Person.Builder builder = AddressProto.Person.newBuilder();
        AddressProto.Person person = builder.setEmail("xxxx").build();
        // 序列化
        byte[] bytes = person.toByteArray();
        //反序列化
        try {
            AddressProto.Person person1 = AddressProto.Person.parseFrom(bytes);
        } catch(InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        //序列化
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            person.writeTo(output);
            byte[] bytes1 = output.toByteArray();
        } catch(IOException e) {
            e.printStackTrace();
        }
        //反序列化
        try {
            AddressProto.Person person1 = AddressProto.Person.parseFrom(new ByteArrayInputStream(bytes));
        } catch(IOException e) {
            e.printStackTrace();
        }
        // Http GRPC
    }
}