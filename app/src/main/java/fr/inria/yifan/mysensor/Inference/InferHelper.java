package fr.inria.yifan.mysensor.Inference;

/*
 This class implements the decision stump (weak learner) for AdaBoost.
 */

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static fr.inria.yifan.mysensor.Support.Configuration.MODEL_INDOOR;
import static fr.inria.yifan.mysensor.Support.Configuration.MODEL_INPOCKET;
import static fr.inria.yifan.mysensor.Support.Configuration.MODEL_UNDERGROUND;

// 1 daytime, 2 light, 3 magnetic, 4 GSM, 5 GPS accuracy, 6 GPS speed, 7 proximity

public class InferHelper {

    private static final String TAG = "Inference helper";

    private Context mContext;
    private AdaBoost mAdaBoostPocket;
    private AdaBoost mAdaBoostDoor;
    private AdaBoost mAdaBoostGround;

    // Initialization for three models
    public InferHelper(Context context) {
        mContext = context;
        // Check local model existence
        File filePocket = mContext.getFileStreamPath(MODEL_INPOCKET);
        File fileDoor = mContext.getFileStreamPath(MODEL_INDOOR);
        File fileGround = mContext.getFileStreamPath(MODEL_UNDERGROUND);

        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;
        FileOutputStream fileOutputStream;
        ObjectOutputStream objectOutputStream;
        if (!filePocket.exists() || !fileDoor.exists() || !fileGround.exists()) {
            Log.d(TAG, "Local model does not exist.");
            // Initialize trained model
            try {
                fileInputStream = mContext.getAssets().openFd(MODEL_INPOCKET).createInputStream();
                objectInputStream = new ObjectInputStream(fileInputStream);
                mAdaBoostPocket = (AdaBoost) objectInputStream.readObject();
                fileInputStream = mContext.getAssets().openFd(MODEL_INDOOR).createInputStream();
                objectInputStream = new ObjectInputStream(fileInputStream);
                mAdaBoostDoor = (AdaBoost) objectInputStream.readObject();
                fileInputStream = mContext.getAssets().openFd(MODEL_UNDERGROUND).createInputStream();
                objectInputStream = new ObjectInputStream(fileInputStream);
                mAdaBoostGround = (AdaBoost) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();
                Log.d(TAG, "Success in loading from file.");

                fileOutputStream = mContext.openFileOutput(MODEL_INPOCKET, Context.MODE_PRIVATE);
                objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(mAdaBoostPocket);
                fileOutputStream = mContext.openFileOutput(MODEL_INDOOR, Context.MODE_PRIVATE);
                objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(mAdaBoostDoor);
                fileOutputStream = mContext.openFileOutput(MODEL_UNDERGROUND, Context.MODE_PRIVATE);
                objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(mAdaBoostGround);
                objectOutputStream.close();
                fileOutputStream.close();
                Log.d(TAG, "Success in saving into file.");
            } catch (Exception e) {
                Log.d(TAG, "Error when loading from file: " + e);
            }
        } else {
            Log.d(TAG, "Local model already exist.");
            try {
                fileInputStream = context.openFileInput(MODEL_INPOCKET);
                objectInputStream = new ObjectInputStream(fileInputStream);
                mAdaBoostPocket = (AdaBoost) objectInputStream.readObject();
                fileInputStream = context.openFileInput(MODEL_INDOOR);
                objectInputStream = new ObjectInputStream(fileInputStream);
                mAdaBoostDoor = (AdaBoost) objectInputStream.readObject();
                fileInputStream = context.openFileInput(MODEL_UNDERGROUND);
                objectInputStream = new ObjectInputStream(fileInputStream);
                mAdaBoostGround = (AdaBoost) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();
                Log.d(TAG, "Success in loading from file.");
            } catch (Exception e) {
                Log.d(TAG, "Error when loading model file: " + e);
            }
        }
    }

    // Inference on In-pocket
    public int InferPocket(double[] sample) {
        assert mAdaBoostPocket != null;
        return mAdaBoostPocket.Predict(sample);
    }

    // Inference on In-door
    public int InferIndoor(double[] sample) {
        assert mAdaBoostDoor != null;
        return mAdaBoostDoor.Predict(sample);
    }

    // Inference on Underground
    public int InferUnderground(double[] sample) {
        assert mAdaBoostGround != null;
        return mAdaBoostGround.Predict(sample);
    }

    // Feedback on wrong inference
    public void FeedBack(double[] sample, String model) {
        switch (model){
            case "Pocket":
                if (mAdaBoostPocket.Predict(sample) != sample[sample.length - 1]) {
                    mAdaBoostPocket.OnlineUpdate(sample);
                    // Save trained model
                    try {
                        FileOutputStream fileOutputStream = mContext.openFileOutput(MODEL_INPOCKET, Context.MODE_PRIVATE);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                        objectOutputStream.writeObject(mAdaBoostPocket);
                        objectOutputStream.close();
                        fileOutputStream.close();
                        Log.d(TAG, "Success in updating model file.");
                    } catch (Exception e) {
                        Log.d(TAG, "Error when updating model file: " + e);
                    }
                }
                break;
            case "Door":
                if (mAdaBoostDoor.Predict(sample) != sample[sample.length - 1]) {
                    mAdaBoostDoor.OnlineUpdate(sample);
                    // Save trained model
                    try {
                        FileOutputStream fileOutputStream = mContext.openFileOutput(MODEL_INDOOR, Context.MODE_PRIVATE);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                        objectOutputStream.writeObject(mAdaBoostPocket);
                        objectOutputStream.close();
                        fileOutputStream.close();
                        Log.d(TAG, "Success in updating model file.");
                    } catch (Exception e) {
                        Log.d(TAG, "Error when updating model file: " + e);
                    }
                }
                break;
            case "Ground":
                if (mAdaBoostGround.Predict(sample) != sample[sample.length - 1]) {
                    mAdaBoostGround.OnlineUpdate(sample);
                    // Save trained model
                    try {
                        FileOutputStream fileOutputStream = mContext.openFileOutput(MODEL_UNDERGROUND, Context.MODE_PRIVATE);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                        objectOutputStream.writeObject(mAdaBoostPocket);
                        objectOutputStream.close();
                        fileOutputStream.close();
                        Log.d(TAG, "Success in updating model file.");
                    } catch (Exception e) {
                        Log.d(TAG, "Error when updating model file: " + e);
                    }
                }
            default:
                Log.e(TAG, "Wrong parameter of model type: " + model);
        }
    }

}
