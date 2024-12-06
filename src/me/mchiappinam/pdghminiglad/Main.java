package me.mchiappinam.pdghminiglad;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

//import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;
//import org.bukkit.scoreboard.Team;

public class Main extends JavaPlugin {
	protected SimpleClans core1;
	protected static Economy econ = null;
	protected Scoreboard sb;
	
	private int minigladEtapa = 0;
	private int diaAutoStart;
	private int horaAutoStart;
	private int minAutoStart;
	protected boolean canStart = true;
	
	protected Location spawn;
	protected Location saida;
	protected Location camarote;
	
	protected HashMap<String,Integer> totalParticipantes = new HashMap<String,Integer>();
	protected List<String> participantes = new ArrayList<String>();
	protected List<String> vips = new ArrayList<String>();
	
	@Override
    public void onEnable() {
		getServer().getConsoleSender().sendMessage("§3[MiniGlad] §2ativando... - Plugin by: mchiappinam");
		getServer().getConsoleSender().sendMessage("§3[MiniGlad] §2Acesse: http://pdgh.com.br/");
		
		if(getServer().getPluginManager().getPlugin("SimpleClans")!=null) {
			getServer().getConsoleSender().sendMessage("§2Hooked to SimpleClans 1!");
			core1=(SimpleClans) getServer().getPluginManager().getPlugin("SimpleClans");
		}
		else {
			getLogger().warning("ERRO: SimpleClans nao encontrado!");
			getServer().getPluginManager().disablePlugin(this);
		}
		
		if(!setupEconomy()) {
			getLogger().warning("ERRO: Vault (Economia) nao encontrado!");
			getServer().getPluginManager().disablePlugin(this);
        }
		else
			getServer().getConsoleSender().sendMessage("§2Hooked to Vault (Economia)!");
		
		getServer().getPluginManager().registerEvents(new Listeners(this), this);
		getServer().getPluginCommand("miniglad").setExecutor(new Comando(this));
		
		File file = new File(getDataFolder(),"config.yml");
		if(!file.exists()) {
			try {
				saveResource("config_template.yml",false);
				File file2 = new File(getDataFolder(),"config_template.yml");
				file2.renameTo(new File(getDataFolder(),"config.yml"));
			}
			catch(Exception e) {}
		}
		
		diaAutoStart = Utils.strToCalendar(getConfig().getString("AutoStart.Dia"));
		getServer().getConsoleSender().sendMessage("§2<> Data automatica:");
		getServer().getConsoleSender().sendMessage("§2Dia = Exceto Domingo - (Default "+diaAutoStart+")");
		horaAutoStart = Integer.parseInt(getConfig().getString("AutoStart.Hora").substring(0,2));
		minAutoStart = Integer.parseInt(getConfig().getString("AutoStart.Hora").substring(2,4));
		getServer().getConsoleSender().sendMessage("§2Hora = "+(horaAutoStart<10?"0"+horaAutoStart:horaAutoStart)+":"+(minAutoStart<10?"0"+minAutoStart:minAutoStart));
		
		String ent[] = getConfig().getString("Arena.Entrada").split(";");
		spawn = new Location(getServer().getWorld(ent[0]),Double.parseDouble(ent[1]),Double.parseDouble(ent[2]),Double.parseDouble(ent[3]),Float.parseFloat(ent[4]),Float.parseFloat(ent[5]));
		String sai[] = getConfig().getString("Arena.Saida").split(";");
		saida = new Location(getServer().getWorld(sai[0]),Double.parseDouble(sai[1]),Double.parseDouble(sai[2]),Double.parseDouble(sai[3]),Float.parseFloat(sai[4]),Float.parseFloat(sai[5]));
		String cam[] = getConfig().getString("Arena.Camarote").split(";");
		camarote = new Location(getServer().getWorld(cam[0]),Double.parseDouble(cam[1]),Double.parseDouble(cam[2]),Double.parseDouble(cam[3]),Float.parseFloat(cam[4]),Float.parseFloat(cam[5]));
		
		getServer().getScheduler().runTaskTimer(this, new Runnable() {
			public void run() {
				//if(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==diaAutoStart)
				if(!(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==1))
					if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)==horaAutoStart)
						if(Calendar.getInstance().get(Calendar.MINUTE)==minAutoStart)
							prepareMiniGlad();
			}
		}, 0, 700);
		
		sb = getServer().getScoreboardManager().getMainScoreboard();
	}
	
	@Override
    public void onDisable() {
		getServer().getConsoleSender().sendMessage("§3[MiniGlad] §2desativado - Plugin by: mchiappinam");
		getServer().getConsoleSender().sendMessage("§3[MiniGlad] §2Acesse: http://pdgh.com.br/");
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	
	protected void prepareMiniGlad() {
		String ent[] = getConfig().getString("Arena.Entrada").split(";");
		spawn = new Location(getServer().getWorld(ent[0]),Double.parseDouble(ent[1]),Double.parseDouble(ent[2]),Double.parseDouble(ent[3]),Float.parseFloat(ent[4]),Float.parseFloat(ent[5]));
		String sai[] = getConfig().getString("Arena.Saida").split(";");
		saida = new Location(getServer().getWorld(sai[0]),Double.parseDouble(sai[1]),Double.parseDouble(sai[2]),Double.parseDouble(sai[3]),Float.parseFloat(sai[4]),Float.parseFloat(sai[5]));
		String cam[] = getConfig().getString("Arena.Camarote").split(";");
		camarote = new Location(getServer().getWorld(cam[0]),Double.parseDouble(cam[1]),Double.parseDouble(cam[2]),Double.parseDouble(cam[3]),Float.parseFloat(cam[4]),Float.parseFloat(cam[5]));
		if(minigladEtapa!=0)
			return;
		getServer().dispatchCommand(getServer().getConsoleSender(), "simpleclans globalff allow");
		minigladEtapa=1;
		me.mchiappinam.pdgheventos.Comandos.cancelarTodosEventos();
		me.mchiappinam.pdgheventos.Comandos.setEvento("API");
		messagePrepare(getConfig().getInt("Timers.Preparar.Avisos"));
	}
	private void messagePrepare(final int vezes) {
		canStart=true;
		if(minigladEtapa!=1)
			return;
		canStart=false;
		if(vezes==0)
			preparedMiniGlad();
		else {
			getServer().broadcastMessage(" ");
			getServer().broadcastMessage("§6§l[MiniGlad] §eEvento miniglad automático começando!");
			getServer().broadcastMessage("§6§l[MiniGlad] §ePara participar digite: §6§l/miniglad");
			getServer().broadcastMessage("§6§l[MiniGlad] §ePremio: §c$"+getConfig().getDouble("Premios.Dinheiro")+" §epara o 1º colocado");
			getServer().broadcastMessage("§6§l[MiniGlad] §eTempo restante: §c"+vezes*getConfig().getInt("Timers.Preparar.TempoEntre")+" segundos");
			getServer().broadcastMessage("§6§l[MiniGlad] §eClans: "+getClansParticipando().size()+" - Jogadores: "+participantes.size());
			getServer().broadcastMessage(" ");
		}
		getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				canStart=true;
				if(minigladEtapa!=1)
					return;
				canStart=false;
				messagePrepare(vezes-1);
			}
		}, 20*getConfig().getInt("Timers.Preparar.TempoEntre"));
	}
	
	
	
	
	
	protected void preparedMiniGlad() {
		if(getClansParticipando().size()<2) {
			cancelMiniGlad();
			cancelMiniGlad();
			getServer().broadcastMessage(" ");
			getServer().broadcastMessage("§6§l[MiniGlad] §eEvento miniglad automático §cCANCELADO!");
			getServer().broadcastMessage("§6§l[MiniGlad] §eMotivo: Quantidade de clans menor que 2");
			getServer().broadcastMessage(" ");
			return;
		}
		minigladEtapa=2;
		getServer().broadcastMessage(" ");
		getServer().broadcastMessage("§6§l[MiniGlad] §eEvento miniglad sendo INICIADO!");
		getServer().broadcastMessage("§6§l[MiniGlad] §eTeleporte para o evento BLOQUEADO!");
		getServer().broadcastMessage(" ");
		canStart=false;
		messageIniciando(getConfig().getInt("Timers.Iniciando.Avisos"));
	}
	private void messageIniciando(final int vezes) {
		canStart=true;
		if(minigladEtapa!=2)
			return;
		canStart=false;
		if(vezes==0)
			startMiniGlad();
		else {
			sendMessageMiniGlad(" ");
			sendMessageMiniGlad("§6§l[MiniGlad] §eEvento miniglad automático começando!");
			sendMessageMiniGlad("§6§l[MiniGlad] §eTempo inicial para os clans se preparar!");
			sendMessageMiniGlad("§6§l[MiniGlad] §eTempo restante: §c"+vezes*getConfig().getInt("Timers.Iniciando.TempoEntre")+" segundos");
			sendMessageMiniGlad(" ");
		}
		getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				canStart=true;
				if(minigladEtapa!=2)
					return;
				canStart=false;
				messageIniciando(vezes-1);
			}
		}, 20*getConfig().getInt("Timers.Iniciando.TempoEntre"));
	}
	
	
	
	
	
	protected void startMiniGlad() {
		canStart=true;
		minigladEtapa=3;
		sendMessageMiniGlad(" ");
		sendMessageMiniGlad("§6§l[MiniGlad] §eVALENDO!");
		sendMessageMiniGlad("§6§l[MiniGlad] §eVALENDO!");
		sendMessageMiniGlad("§6§l[MiniGlad] §eVALENDO!");
		sendMessageMiniGlad(" ");
	}
	
	
	
	
	
	protected void checkMiniGladEnd() {
		List<Clan> lista = getClansParticipando();
		if(lista.size()==1)
			if(minigladEtapa==3) {
				minigladEtapa=4;
				Clan vencedor = lista.get(0);
				me.mchiappinam.pdgheventos.Comandos.setEvento("nenhum");
				//double premio = (getConfig().getDouble("Premios.Dinheiro")*1.0)/vencedor.getLeaders().size();
				//for(ClanPlayer cp : vencedor.getLeaders())
				//	econ.depositPlayer(cp.getName(), premio);
				String v1 = null;
				int v1_v = -1;
				String v2 = null;
				int v2_v = -1;
				String v3 = null;
				int v3_v = -1;
				for(String n : totalParticipantes.keySet())
					if(core1.getClanManager().getClanByPlayerName(n)==vencedor) {
						int matou = totalParticipantes.get(n);
						if(matou>v1_v) {
							v3_v=v2_v;
							v3=v2;
							v2_v=v1_v;
							v2=v1;
							v1=n;
							v1_v=matou;
						}
						else if(matou>v2_v) {
							v3=v2;
							v3_v=v2_v;
							v2=n;
							v2_v=matou;
						}
						else if(matou>v2_v) {
							v3=n;
							v3_v=matou;
						}
					}
				getServer().broadcastMessage(" ");
				getServer().broadcastMessage("§6§l[MiniGlad] §eEvento MiniGlad FINALIZADO!");
				getServer().broadcastMessage("§6§l[MiniGlad] §eClan vencedor: §l"+vencedor.getName());
				getServer().broadcastMessage("§6§l[MiniGlad] §ePremio: §c$"+getConfig().getDouble("Premios.Dinheiro")+" §epara o 1º colocado");
				getServer().broadcastMessage("§6§l[MiniGlad] §e1º colocado: "+v1+" ("+v1_v+")");
				if(v2!=null) {
				    getServer().broadcastMessage("§6§l[MiniGlad] §e2º colocado: "+v2+" ("+v2_v+")");
				    getServer().broadcastMessage((v3!=null?"§6§l[MiniGlad] §e3º colocado: "+v3+" ("+v3_v+")":""));
				}
				getServer().broadcastMessage(" ");
				econ.depositPlayer(v1, getConfig().getDouble("Premios.Dinheiro"));
				sendMessageMiniGlad("§6§l[MiniGlad] §b§lVocê tem "+getConfig().getInt("Timers.Finalizando")+" segundos para recolher os itens do evento MiniGlad!");
				getServer().dispatchCommand(getServer().getConsoleSender(), "simpleclans globalff auto");
				getServer().getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						finalizarMiniGlad();
					}
				}, 20*getConfig().getInt("Timers.Finalizando"));
			}
	}
	
	
	
	
	protected void finalizarMiniGlad() {
		sendMessageMiniGlad(" ");
		sendMessageMiniGlad("§6§l[MiniGlad] §eTempo esgotado!");
		sendMessageMiniGlad("§6§l[MiniGlad] §eFim do evento!");
		sendMessageMiniGlad(" ");
		cancelMiniGlad();
	}
	
	protected void cancelMiniGlad() {
		if(minigladEtapa==0)
			return;
		getServer().dispatchCommand(getServer().getConsoleSender(), "simpleclans globalff auto");
		minigladEtapa=0;
		for(String n : participantes) {
			getServer().getPlayer(n).teleport(saida);
		}
		participantes.clear();
		vips.clear();
		totalParticipantes.clear();
		me.mchiappinam.pdgheventos.Comandos.setEvento("nenhum");
	}
	
	protected int getMiniGladEtapa() {
		return minigladEtapa;
	}
	
	protected void addPlayer(Player p) {
		totalParticipantes.put(p.getName(), 0);
		participantes.add(p.getName());
		p.teleport(spawn);
		p.sendMessage(" ");
		p.sendMessage("§6§l[MiniGlad] §eVocê entrou no evento miniglad!");
		p.sendMessage("§6§l[MiniGlad] §cPara sair digite: §c§l/miniglad sair");
		p.sendMessage("§6§l[MiniGlad] §eAgrupe-se com seu clan enquanto o evento está iniciando!");
		p.sendMessage(" ");
		clearInv(p);
		Kit(p);
		p.setFoodLevel(20);
		//p.setHealth(20);
	    for(PotionEffect effect : p.getActivePotionEffects()) {
	    	p.removePotionEffect(effect.getType());
	    	p.sendMessage("§6§l[MiniGlad] §ePoção §6"+effect.getType().getName()+" §eremovida.");
	    }
	}
	
	public void clearInv(Player p) {
		p.closeInventory();
		p.closeInventory();
		p.closeInventory();
		p.closeInventory();
		p.closeInventory();
		p.getInventory().setHelmet(null);
		p.getInventory().setChestplate(null);
		p.getInventory().setLeggings(null);
		p.getInventory().setBoots(null);
		p.getInventory().clear();
	}
	
	public void Kit(Player p) {
		if(p.hasPermission("pdgh.vip")) {
			ItemStack espada = new ItemStack(Material.DIAMOND_SWORD, 1);
			espada.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
			espada.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
			ItemStack arco = new ItemStack(Material.BOW, 1);
			arco.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE , 5);
			arco.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
			arco.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
			arco.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			ItemStack elmo = new ItemStack(Material.DIAMOND_HELMET, 1);
			elmo.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 6);
			elmo.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
			ItemStack peito = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
			peito.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 6);
			peito.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
			ItemStack calca = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
			calca.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 6);
			calca.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
			ItemStack bota = new ItemStack(Material.DIAMOND_BOOTS, 1);
			bota.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 6);
			bota.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
			p.getInventory().addItem(espada);
			p.getInventory().addItem(arco);
			p.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 30, (short) 1));
			p.getInventory().addItem(new ItemStack(Material.POTION, 20, (short) 8233));
			p.getInventory().addItem(new ItemStack(Material.POTION, 20, (short) 8226));
			p.getInventory().addItem(elmo);
			p.getInventory().addItem(peito);
			p.getInventory().addItem(calca);
			p.getInventory().addItem(bota);
			p.getInventory().setHelmet(elmo);
			p.getInventory().setChestplate(peito);
			p.getInventory().setLeggings(calca);
			p.getInventory().setBoots(bota);
			p.getInventory().addItem(new ItemStack(Material.ARROW, 1));
			
			if(!vips.contains(p.getName().toLowerCase())) {
				getServer().broadcastMessage("§6§l[MiniGlad] §6§l"+p.getName()+" §eé VIP e ganhou em toda sua armadura +2 leveis de encantamento e o dobro de todas as poções.");
				vips.add(p.getName().toLowerCase());
			}
			
		}else{
			ItemStack espada = new ItemStack(Material.DIAMOND_SWORD, 1);
			espada.addEnchantment(Enchantment.DAMAGE_ALL, 5);
			espada.addEnchantment(Enchantment.FIRE_ASPECT, 2);
			ItemStack arco = new ItemStack(Material.BOW, 1);
			arco.addEnchantment(Enchantment.ARROW_DAMAGE , 5);
			arco.addEnchantment(Enchantment.ARROW_FIRE, 1);
			arco.addEnchantment(Enchantment.ARROW_INFINITE, 1);
			arco.addEnchantment(Enchantment.DURABILITY, 3);
			ItemStack elmo = new ItemStack(Material.DIAMOND_HELMET, 1);
			elmo.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 4);
			elmo.addEnchantment(Enchantment.DURABILITY, 3);
			ItemStack peito = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
			peito.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 4);
			peito.addEnchantment(Enchantment.DURABILITY, 3);
			ItemStack calca = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
			calca.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 4);
			calca.addEnchantment(Enchantment.DURABILITY, 3);
			ItemStack bota = new ItemStack(Material.DIAMOND_BOOTS, 1);
			bota.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 4);
			bota.addEnchantment(Enchantment.DURABILITY, 3);
			p.getInventory().addItem(espada);
			p.getInventory().addItem(arco);
			p.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 30, (short) 1));
			p.getInventory().addItem(new ItemStack(Material.POTION, 10, (short) 8233));
			p.getInventory().addItem(new ItemStack(Material.POTION, 10, (short) 8226));
			p.getInventory().addItem(new ItemStack(Material.ARROW, 1));
			p.getInventory().addItem(elmo);
			p.getInventory().addItem(peito);
			p.getInventory().addItem(calca);
			p.getInventory().addItem(bota);
			p.getInventory().setHelmet(elmo);
			p.getInventory().setChestplate(peito);
			p.getInventory().setLeggings(calca);
			p.getInventory().setBoots(bota);
		}
	}
	
	protected void removePlayer(Player p,int motive) {//0=sair, 1=morrer, 2=quit, 3=kick
		if(!participantes.contains(p.getName()))
			return;
		participantes.remove(p.getName());
		if(minigladEtapa<2)
			totalParticipantes.remove(p.getName());
		else if(minigladEtapa==3) {
			if(getClansParticipando().size()>1)
				getServer().broadcastMessage("§6§l[MiniGlad] §eRestam "+getClansParticipando().size()+" clans e "+participantes.size()+" jogadores dentro do miniglad!");
			checkMiniGladEnd();
		}
		if(minigladEtapa==1) {
			totalParticipantes.remove(p.getName());
			if(motive!=3) {
				p.teleport(saida);
				p.sendMessage("§6§l[MiniGlad] §eVocê saiu do evento miniglad, para voltar: §c/miniglad");
			}else{
				p.teleport(saida);
				p.sendMessage("§6§l[MiniGlad] §cVocê foi kickado do evento miniglad");
			}
		}
		else {
			if(motive==0) {
				p.teleport(saida);
				p.sendMessage("§6§l[MiniGlad] §eVocê saiu do evento miniglad");
			}else if(motive==1)
				p.sendMessage("§6§l[MiniGlad] §eVocê morreu no evento miniglad");
			else if(motive==3) {
				p.teleport(saida);
				p.sendMessage("§6§l[MiniGlad] §cVocê foi kickado do evento miniglad");
			}
		}
	}
	
	protected List<Clan> getClansParticipando() {
		List<Clan> clans = new ArrayList<Clan>();
		for(String n : participantes) {
			ClanPlayer cp = core1.getClanManager().getClanPlayer(getServer().getPlayer(n));
			if(cp!=null)
				if(!clans.contains(cp.getClan()))
					clans.add(cp.getClan());
		}
		return clans;
	}
	
	protected void sendMessageMiniGlad(String msg) {
		for(String n : participantes)
			getServer().getPlayer(n).sendMessage(msg);
	}
	
}
