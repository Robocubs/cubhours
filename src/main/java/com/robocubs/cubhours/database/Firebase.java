/*
 * MIT License
 *
 * Copyright 2020-2021 noahhusby
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.robocubs.cubhours.database;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class Firebase implements Closeable {

    @Getter
    private Firestore database;

    public Firebase(@NonNull File credentials) throws IOException {
        FileInputStream credentialsStream = new FileInputStream(credentials);
        FirestoreOptions firestoreOptions =
                FirestoreOptions.getDefaultInstance().toBuilder()
                        .setProjectId(FirestoreOptions.getDefaultProjectId())
                        .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                        .build();
        credentialsStream.close();
        database = firestoreOptions.getService();
    }


    public void addCollection(@NonNull String collection, @NonNull Object data) {
        CollectionReference reference = database.collection(collection);
        reference.add(data);
    }

    public void setDocument(@NonNull String collection, @NonNull String document, @NonNull Object data) {
        DocumentReference docRef = database.collection(collection).document(document);
        docRef.set(data);
    }

    public void removeCollection(@NonNull String collection) {
        CollectionReference reference = database.collection(collection);
        for (DocumentReference document : reference.listDocuments()) {
            document.delete();
        }
    }

    public void removeDocument(@NonNull String collection, @NonNull String document) {
        CollectionReference reference = database.collection(collection);
        reference.document(document).delete();
    }

    public DocumentReference getDocument(@NonNull String collection, @NonNull String document) {
        return database.collection(collection).document(document);
    }

    public <T> T getDocumentAs(@NonNull String collection, @NonNull String document, @NonNull Class<T> clazz) throws Exception {
        DocumentReference docRef = getDocument(collection, document);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot documentSnapshot = future.get();
        if (documentSnapshot.exists()) {
            return documentSnapshot.toObject(clazz);
        }
        return null;
    }

    @SneakyThrows
    public List<QueryDocumentSnapshot> getDocuments(@NonNull String collection) {
        ApiFuture<QuerySnapshot> future = database.collection(collection).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents;
    }

    @SneakyThrows
    public boolean doesCollectionExist(@NonNull String collection) {
        QuerySnapshot snapshot = database.collection(collection).get().get();
        return !snapshot.isEmpty();
    }

    @Override
    @SneakyThrows
    public void close() {
        database.close();
    }
}
