package com.android.mediapipe.service

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi

import androidx.core.app.NotificationCompat
import com.android.mediapipe.FirstPageActivity
import com.android.mediapipe.R


import java.io.FileDescriptor
import java.io.PrintWriter
import java.util.*


class SmartAutoClickerService : AccessibilityService() {

    companion object {
        var upscroll = 60;
        var downscroll = 60;
        var tap = 40
        /** The identifier for the foreground notification of this service. */
        private const val NOTIFICATION_ID = 42

        /** The channel identifier for the foreground notification of this service. */
        private const val NOTIFICATION_CHANNEL_ID = "SmartAutoClickerService"

        /** The instance of the [LocalService], providing access for this service to the Activity. */
        private var LOCAL_SERVICE_INSTANCE: LocalService? = null
            set(value) {
                field = value
                LOCAL_SERVICE_CALLBACK?.invoke(field)
            }

        /** Callback upon the availability of the [LOCAL_SERVICE_INSTANCE]. */
        private var LOCAL_SERVICE_CALLBACK: ((LocalService?) -> Unit)? = null
            set(value) {
                field = value
                value?.invoke(LOCAL_SERVICE_INSTANCE)
            }

        //        fun getLocalService(stateCallback: ((LocalService?) -> Unit)?) {
//                    LOCAL_SERVICE_CALLBACK = stateCallback
//        }
        fun getLocalServiceInstance(): LocalService? {
            return LOCAL_SERVICE_INSTANCE
        }
        private var isStarted: Boolean = false
        fun getStatus(): Boolean {
            return isStarted
        }


    }



    /** Local interface providing an API for the [SmartAutoClickerService]. */
    inner class LocalService {


        private fun findScorllableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
            val deque: Deque<AccessibilityNodeInfo> = ArrayDeque()
            deque.add(root)
            while (!deque.isEmpty()) {
                val node = deque.removeFirst()
                if (node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                    return node
                }
                for (i in 0 until node.childCount) {
                    deque.addLast(node.getChild(i))
                }
            }
            return null
        }
        private fun findScreenTapNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
            val deque: Deque<AccessibilityNodeInfo> = ArrayDeque()
            deque.add(root)
            while (!deque.isEmpty()) {

                val node = deque.removeFirst()
                Log.d("Nodeinfo", "findScreenTapNode: "+node.windowId+"  "+node.className)
                if (node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) {
                    return node
                }
                for (i in 0 until node.childCount) {
                    deque.addLast(node.getChild(i))
                }
            }
            return null
        }

        fun configureScrollButtonUp() {
            if (upscroll == 0) {

                Log.d("CheckScroooo", "configureScrollButton: $rootInActiveWindow")
                val scrollable = findScorllableNode(rootInActiveWindow)

                if (scrollable != null && scrollable.isScrollable) {
                    scrollable
                        .performAction(
                            AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD
                                .id
                        )
                }
                upscroll = 5;
            } else {
                upscroll--;
            }
        }
        fun configTap(){
            if (tap == 0) {

//                Log.d("CheckScroooo", "configureScrollButton: $rootInActiveWindow")
                val tapable = findScreenTapNode(rootInActiveWindow)
                if (tapable != null && tapable.isClickable) {
                    tapable
                        .performAction(
                            AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK
                                .id
                        )
                }
                tap = 5
            } else {
                tap--
            }
        }

        fun configureScrollButtonDown() {
            if (downscroll == 0) {
                Log.d("CheckScroooo", "configureScrollButton: $rootInActiveWindow")
                val scrollable = findScorllableNode(rootInActiveWindow)
                if (scrollable != null && scrollable.isScrollable) {
                    scrollable
                        .performAction(
                            AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD
                                .id
                        )
                }
                downscroll = 5
            }
            else{
                downscroll--
            }
        }


        @RequiresApi(Build.VERSION_CODES.M)
        fun start() {
            if (isStarted) {
                return
            }

            isStarted = true
            Log.d("rrgg", "start: forground")
            startForeground(NOTIFICATION_ID, createNotification("Mediapipe"))

        }

        fun stop() {
            if (!isStarted) {
                return
            }

            isStarted = false


            stopForeground(true)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        LOCAL_SERVICE_INSTANCE = LocalService()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        LOCAL_SERVICE_INSTANCE?.stop()
        LOCAL_SERVICE_INSTANCE = null
        isStarted = false
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        isStarted = false
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun createNotification(scenarioName: String): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager!!.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        val intent = Intent(this, FirstPageActivity::class.java)
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Gesture Service ")
            .setContentText(getString(R.string.notification_message))
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }



    override fun onInterrupt() { /* Unused */
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* Unused */
    }


}