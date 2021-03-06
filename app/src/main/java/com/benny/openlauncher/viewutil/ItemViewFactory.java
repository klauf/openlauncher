package com.benny.openlauncher.viewutil;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.widget.AppItemView;
import com.benny.openlauncher.widget.CellContainer;
import com.benny.openlauncher.widget.WidgetView;

/**
 * Created by BennyKok on 10/24/2016.
 */

public class ItemViewFactory {

    public static final int NO_FLAGS = 0x01;
    public static final int NO_LABEL = 0x02;

    public static View getItemView(final Context context, final DesktopCallBack callBack, final Item item, int flags) {
        View view = null;
        if (item.type == null) {
            return null;
        }
        switch (item.type) {
            case APP:
                final AppManager.App app = AppManager.getInstance(context).findApp(item.appIntent.getComponent().getPackageName(), item.appIntent.getComponent().getClassName());
                if (app == null) {
                    break;
                }
                view = new AppItemView.Builder(context)
                        .setAppItem(app)
                        .withOnClickLaunchApp(app)
                        .withOnTouchGetPosition()
                        .vibrateWhenLongPress()
                        .withOnLongPressDrag(item, DragAction.Action.APP, new AppItemView.Builder.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                return true;
                            }

                            @Override
                            public void afterDrag(View view) {
                                callBack.setLastItem(item, view);
                            }
                        })
                        .setLabelVisibility((flags & NO_LABEL) != NO_LABEL)
                        .setTextColor(Color.WHITE)
                        .getView();
                break;
            case SHORTCUT:
                view = new AppItemView.Builder(context)
                        .setShortcutItem(item.appIntent)
                        .withOnTouchGetPosition()
                        .vibrateWhenLongPress()
                        .withOnLongPressDrag(item, DragAction.Action.SHORTCUT, new AppItemView.Builder.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                return true;
                            }

                            @Override
                            public void afterDrag(View view) {
                                callBack.setLastItem(item, view);
                            }
                        })
                        .setLabelVisibility((flags & NO_LABEL) != NO_LABEL)
                        .setTextColor(Color.WHITE)
                        .getView();
                break;
            case GROUP:
                view = new AppItemView.Builder(context)
                        .withOnTouchGetPosition()
                        .vibrateWhenLongPress()
                        .withOnLongPressDrag(item, DragAction.Action.GROUP, new AppItemView.Builder.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                return true;
                            }

                            @Override
                            public void afterDrag(View view) {
                                callBack.setLastItem(item, view);
                            }
                        })
                        .setLabelVisibility((flags & NO_LABEL) != NO_LABEL)
                        .setTextColor(Color.WHITE)
                        .getView();
                view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                ((AppItemView) view).setIcon(getGroupIconDrawable(context, item));
                ((AppItemView) view).setLabel((item.name));
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Home.launcher != null && Home.launcher.groupPopup.showWindowV(item, v, callBack)) {
                            ((GroupIconDrawable) ((AppItemView) v).getIcon()).popUp();
                        }
                    }
                });
                break;
            case ACTION:
                view = new AppItemView.Builder(context)
                        .setActionItem(item)
                        .withOnTouchGetPosition()
                        .vibrateWhenLongPress()
                        .withOnLongPressDrag(item, DragAction.Action.ACTION, new AppItemView.Builder.LongPressCallBack() {
                            @Override
                            public boolean readyForDrag(View view) {
                                return true;
                            }

                            @Override
                            public void afterDrag(View view) {
                                callBack.setLastItem(item, view);
                            }
                        })
                        .setLabelVisibility((flags & NO_LABEL) != NO_LABEL)
                        .setTextColor(Color.WHITE)
                        .getView();
                break;
            case WIDGET:
                final AppWidgetProviderInfo appWidgetInfo = Home.appWidgetManager.getAppWidgetInfo(item.widgetID);
                final WidgetView widgetView = (WidgetView) Home.appWidgetHost.createView(context, item.widgetID, appWidgetInfo);

                widgetView.setAppWidget(item.widgetID, appWidgetInfo);
                widgetView.post(new Runnable() {
                    @Override
                    public void run() {
                        updateWidgetOption(item);
                    }
                });

                final FrameLayout widgetContainer = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.view_widget_container, null);
                widgetContainer.addView(widgetView);

                final View ve = widgetContainer.findViewById(R.id.vertexpand);
                ve.bringToFront();
                final View he = widgetContainer.findViewById(R.id.horiexpand);
                he.bringToFront();
                final View vl = widgetContainer.findViewById(R.id.vertless);
                vl.bringToFront();
                final View hl = widgetContainer.findViewById(R.id.horiless);
                hl.bringToFront();

                ve.animate().scaleY(1).scaleX(1);
                he.animate().scaleY(1).scaleX(1);
                vl.animate().scaleY(1).scaleX(1);
                hl.animate().scaleY(1).scaleX(1);

                final Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        ve.animate().scaleY(0).scaleX(0);
                        he.animate().scaleY(0).scaleX(0);
                        vl.animate().scaleY(0).scaleX(0);
                        hl.animate().scaleY(0).scaleX(0);
                    }
                };

                widgetContainer.postDelayed(action, 2000);
                widgetView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (AppSettings.get().isDesktopLocked())
                            return false;

                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        Intent i = new Intent();
                        i.putExtra("mDragData", item);
                        ClipData data = ClipData.newIntent("mDragIntent", i);
                        view.startDrag(data, new GoodDragShadowBuilder(view), new DragAction(DragAction.Action.WIDGET), 0);

                        callBack.setLastItem(item, widgetContainer);
                        return true;
                    }
                });

                ve.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getScaleX() < 1) return;
                        item.spanY++;
                        scaleWidget(widgetContainer, item);
                        widgetContainer.removeCallbacks(action);
                        widgetContainer.postDelayed(action, 2000);
                    }
                });
                he.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getScaleX() < 1) return;
                        item.spanX++;
                        scaleWidget(widgetContainer, item);
                        widgetContainer.removeCallbacks(action);
                        widgetContainer.postDelayed(action, 2000);
                    }
                });
                vl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getScaleX() < 1) return;
                        item.spanY--;
                        int minY = ((appWidgetInfo.minResizeHeight - 1) / Home.launcher.desktop.pages.get(Home.launcher.desktop.getCurrentItem()).cellHeight) + 1;
                        item.spanY = Math.max(item.spanY, minY);
                        scaleWidget(widgetContainer, item);
                        widgetContainer.removeCallbacks(action);
                        widgetContainer.postDelayed(action, 2000);
                    }
                });
                hl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getScaleX() < 1) return;
                        item.spanX--;
                        int minX = ((appWidgetInfo.minResizeWidth - 1) / Home.launcher.desktop.pages.get(Home.launcher.desktop.getCurrentItem()).cellWidth) + 1;
                        item.spanX = Math.max(item.spanX, minX);
                        scaleWidget(widgetContainer, item);
                        widgetContainer.removeCallbacks(action);
                        widgetContainer.postDelayed(action, 2000);
                    }
                });
                view = widgetContainer;
                break;
        }
        if (view != null) {
            view.setTag(item);
        }
        return view;
    }

    private static void scaleWidget(View view, Item item) {
        item.spanX = Math.min(item.spanX, 4);
        item.spanX = Math.max(item.spanX, 1);
        item.spanY = Math.min(item.spanY, 4);
        item.spanY = Math.max(item.spanY, 1);

        CellContainer.LayoutParams cellPositionToLayoutParams = null;
        if (Home.launcher.desktop.pages.get(Home.launcher.desktop.getCurrentItem()).checkOccupied(item.x, item.y, item.spanX, item.spanY, (CellContainer.LayoutParams) view.getLayoutParams())) {
            cellPositionToLayoutParams = new CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT, CellContainer.LayoutParams.WRAP_CONTENT, item.x, item.y, item.spanX, item.spanY);
        }

        if (cellPositionToLayoutParams == null) {
            Toast.makeText(Home.launcher.desktop.getContext(), R.string.toast_not_enough_space, Toast.LENGTH_SHORT).show();
        } else {
            item.x = cellPositionToLayoutParams.x;
            item.y = cellPositionToLayoutParams.y;
            view.setLayoutParams(cellPositionToLayoutParams);
            updateWidgetOption(item);

            // update the widget size in the database
            Home.db.updateItem(item);
        }
    }

    private static void updateWidgetOption(Item item) {
        Bundle newOps = new Bundle();
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, item.spanX * Home.launcher.desktop.pages.get(Home.launcher.desktop.getCurrentItem()).cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, item.spanX * Home.launcher.desktop.pages.get(Home.launcher.desktop.getCurrentItem()).cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, item.spanX * Home.launcher.desktop.pages.get(Home.launcher.desktop.getCurrentItem()).cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, item.spanY * Home.launcher.desktop.pages.get(Home.launcher.desktop.getCurrentItem()).cellHeight);
        Home.appWidgetManager.updateAppWidgetOptions(item.widgetID, newOps);
    }

    public static Drawable getGroupIconDrawable(Context context, Item item) {
        final float iconSize = Tool.dp2px(AppSettings.get().getIconSize(), context);
        final Bitmap[] icons = new Bitmap[4];
        for (int i = 0; i < 4; i++) {
            if (i < item.items.size()) {
                if (item.items.get(i).appIntent.getStringExtra("shortCutIconID") != null) {
                    icons[i] = Tool.drawableToBitmap(Tool.getIconFromID(context, item.items.get(i).appIntent.getStringExtra("shortCutIconID")));
                } else {
                    AppManager.App app = AppManager.getInstance(context).findApp(item.items.get(i).appIntent.getComponent().getPackageName(), item.items.get(i).appIntent.getComponent().getClassName());
                    if (app != null) {
                        icons[i] = Tool.drawableToBitmap(app.icon);
                    } else {
                        icons[i] = Tool.drawableToBitmap(new ColorDrawable(Color.TRANSPARENT));
                    }
                }
            } else {
                icons[i] = Tool.drawableToBitmap(new ColorDrawable(Color.TRANSPARENT));
            }
        }
        return new GroupIconDrawable(icons, iconSize);
    }
}
