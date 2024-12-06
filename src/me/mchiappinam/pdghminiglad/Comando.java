package me.mchiappinam.pdghminiglad;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Comando implements CommandExecutor {
	private Main plugin;
	public Comando(Main main) {
		plugin=main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("miniglad")) {
			if(args.length==0) {
				/**if(!sender.hasPermission("pdgh.vip")) {
					sender.sendMessage("§6§l[MiniGlad] §cApenas §6§lVIP §cpode entrar no evento miniglad!");
					sender.sendMessage("§6§l[MiniGlad] §cAdquira seu §6§lVIP §cem www.pdgh.net");
					return true;
				}*/
				if(sender==plugin.getServer().getConsoleSender()) {
					sender.sendMessage("§6§l[MiniGlad] §cConsole bloqueado de executar o comando!");
					return true;
				}
				if(plugin.getMiniGladEtapa()==0) {
					sender.sendMessage("§6§l[MiniGlad] §cO evento miniglad não está acontecendo!");
					return true;
				}
				if(plugin.getMiniGladEtapa()>1) {
					sender.sendMessage("§6§l[MiniGlad] §cO evento miniglad já começou!");
					return true;
				}
				if(plugin.participantes.contains(sender.getName())) {
					sender.sendMessage("§6§l[MiniGlad] §cVocê já entrou no evento miniglad!");
					return true;
				}
				if(plugin.getConfig().contains("Bans."+sender.getName().toLowerCase())) {
					sender.sendMessage("§6§l[MiniGlad] §cVocê está banido do evento miniglad!");
					sender.sendMessage("§6§l[MiniGlad] §cBanido por "+plugin.getConfig().getString("Bans."+sender.getName().toLowerCase()+".Por")+" em "+plugin.getConfig().getString("Bans."+sender.getName().toLowerCase()+".Data"));
					return true;
				}
				if(plugin.core1.getClanManager().getClanPlayer((Player)sender)==null) {
					sender.sendMessage("§6§l[MiniGlad] §cVocê não tem clan!");
					return true;
				}
                if(((Player)sender).isInsideVehicle()) {
				     sender.sendMessage("§6§l[MiniGlad] §cVocê está dentro de um veículo!");
				     return true;
				}
                if(((Player)sender).isDead()) {
				     sender.sendMessage("§6§l[MiniGlad] §cVocê está morto!");
				     return true;
				}
				plugin.addPlayer((Player)sender);
				return true;
			}
			else {
				if(args[0].equalsIgnoreCase("sair")) {
					if(plugin.getMiniGladEtapa()==0) {
						sender.sendMessage("§6§l[MiniGlad] §cO evento miniglad não está aberto!");
						return true;
					}
					if(plugin.getMiniGladEtapa()!=1) {
						sender.sendMessage("§6§l[MiniGlad] §cVocê não pode sair agora!");
						return true;
					}
					plugin.removePlayer((Player)sender,0);
					return true;
				}
				if(args[0].equalsIgnoreCase("camarote")) {
					if(!sender.hasPermission("miniglad.camarote")) {
						sender.sendMessage("§6§l[MiniGlad] §cVocê não tem permissão para executar esse comando!");
						return true;
					}
					if(plugin.getMiniGladEtapa()==0) {
						sender.sendMessage("§6§l[MiniGlad] §cO evento miniglad não está acontecendo!");
						return true;
					}
					if(plugin.participantes.contains(sender.getName())) {
						sender.sendMessage("§6§l[MiniGlad] §cVocê não pode ir para o camarote participando do evento miniglad!");
						return true;
					}
					((Player)sender).teleport(plugin.camarote);
					sender.sendMessage("§6§l[MiniGlad] §oVocê foi para o camarote do miniglad!");
					return true;
				}
				//outro cmds, admin!
				if(!sender.hasPermission("pdgh.admin")) {
					sender.sendMessage("§6§l[MiniGlad] §cVocê não tem permissão para executar esse comando!");
					return true;
				}
				if(args[0].equalsIgnoreCase("forcestart")) {
					if(plugin.getMiniGladEtapa()!=0) {
						sender.sendMessage("§6§l[MiniGlad] §cJá existe um evento miniglad sendo executado!");
						return true;
					}
					if(plugin.getMiniGladEtapa()==0&&!plugin.canStart) {
						sender.sendMessage("§6§l[MiniGlad] §cUm evento miniglad está sendo finalizado!");
						return true;
					}
					sender.sendMessage("§6§l[MiniGlad] §oEvento miniglad sendo iniciado!");
					plugin.prepareMiniGlad();
					return true;
				}
				if(args[0].equalsIgnoreCase("forcestop")) {
					if(plugin.getMiniGladEtapa()==0) {
						sender.sendMessage("§6§l[MiniGlad] §cNão há nenhum evento miniglad sendo executado!");
						return true;
					}
					plugin.cancelMiniGlad();
					sender.sendMessage("§6§l[MiniGlad] §oEvento miniglad sendo parado!");
					return true;
				}
				if(args[0].equalsIgnoreCase("kick")) {
					if(args.length<2) {
						sender.sendMessage("§6§l[MiniGlad] §c/miniglad kick <nome>");
						return true;
					}
					String nome = args[1].toLowerCase();
					Player p = plugin.getServer().getPlayer(nome);
					if(p==null) {
						sender.sendMessage("§6§l[MiniGlad] §cJogador não encontrado!");
						return true;
					}
					plugin.removePlayer(p, 3);
					sender.sendMessage("§6§l[MiniGlad] §o"+nome+" foi kickado do evento miniglad!");
					return true;
				}
				if(args[0].equalsIgnoreCase("info")) {
					if(plugin.getMiniGladEtapa()!=3) {
						sender.sendMessage("§6§l[MiniGlad] §cO evento miniglad não está acontecendo!");
						return true;
					}
					sender.sendMessage("§6§l[MiniGlad] Restam "+plugin.getClansParticipando().size()+" clans e "+plugin.participantes.size()+" jogadores dentro do miniglad!");
					return true;
				}
				if(args[0].equalsIgnoreCase("ban")) {
					if(args.length<2) {
						sender.sendMessage("§6§l[MiniGlad] §c/miniglad ban <nome>");
						return true;
					}
					String nome = args[1].toLowerCase();
					plugin.getConfig().set("Bans."+nome+".Por", sender.getName());
					plugin.getConfig().set("Bans."+nome+".Data", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
					plugin.saveConfig();
					Player p = plugin.getServer().getPlayerExact(nome);
					if(p!=null)
						plugin.removePlayer(p, 3);
					sender.sendMessage("§6§l[MiniGlad] §o"+nome+" foi banido do evento miniglad!");
					return true;
				}
				if(args[0].equalsIgnoreCase("unban")) {
					if(args.length<2) {
						sender.sendMessage("§6§l[MiniGlad] §c/miniglad ban <nome>");
						return true;
					}
					String nome = args[1].toLowerCase();
					if(!plugin.getConfig().contains("Bans."+nome)) {
						sender.sendMessage("§6§l[MiniGlad] §cNome não encontrado!");
						return true;
					}
					plugin.getConfig().set("Bans."+nome, null);
					plugin.saveConfig();
					sender.sendMessage("§6§l[MiniGlad] §o"+nome+" foi desbanido do evento miniglad!");
					return true;
				}
				if(args[0].equalsIgnoreCase("setspawn")) {
					if(sender==plugin.getServer().getConsoleSender()) {
						sender.sendMessage("§6§l[MiniGlad] §cConsole bloqueado de executar o comando!");
						return true;
					}
					Player p = (Player)sender;
					plugin.spawn=p.getLocation();
					plugin.getConfig().set("Arena.Entrada", plugin.spawn.getWorld().getName()+";"+plugin.spawn.getX()+";"+plugin.spawn.getY()+";"+plugin.spawn.getZ()+";"+plugin.spawn.getYaw()+";"+plugin.spawn.getPitch());
					plugin.saveConfig();
					sender.sendMessage("§6§l[MiniGlad] §oSpawn marcado!");
					return true;
				}
				if(args[0].equalsIgnoreCase("setsaida")) {
					if(sender==plugin.getServer().getConsoleSender()) {
						sender.sendMessage("§6§l[MiniGlad] §cConsole bloqueado de executar o comando!");
						return true;
					}
					Player p = (Player)sender;
					plugin.saida=p.getLocation();
					plugin.getConfig().set("Arena.Saida", plugin.saida.getWorld().getName()+";"+plugin.saida.getX()+";"+plugin.saida.getY()+";"+plugin.saida.getZ()+";"+plugin.saida.getYaw()+";"+plugin.saida.getPitch());
					plugin.saveConfig();
					sender.sendMessage("§6§l[MiniGlad] §oSaída marcada!");
					return true;
				}
				if(args[0].equalsIgnoreCase("setcamarote")) {
					if(sender==plugin.getServer().getConsoleSender()) {
						sender.sendMessage("§6§l[MiniGlad] §cConsole bloqueado de executar o comando!");
						return true;
					}
					Player p = (Player)sender;
					plugin.camarote=p.getLocation();
					plugin.getConfig().set("Arena.Camarote", plugin.camarote.getWorld().getName()+";"+plugin.camarote.getX()+";"+plugin.camarote.getY()+";"+plugin.camarote.getZ()+";"+plugin.camarote.getYaw()+";"+plugin.camarote.getPitch());
					plugin.saveConfig();
					sender.sendMessage("§6§l[MiniGlad] §oCamarote marcado!");
					return true;
				}
				if(args[0].equalsIgnoreCase("reload")) {
					if(plugin.getMiniGladEtapa()!=0) {
						sender.sendMessage("§6§l[MiniGlad] §cHá um evento miniglad acontecendo!");
						return true;
					}
					plugin.reloadConfig();
					String ent[] = plugin.getConfig().getString("Arena.Entrada").split(";");
					plugin.spawn = new Location(plugin.getServer().getWorld(ent[0]),Double.parseDouble(ent[1]),Double.parseDouble(ent[2]),Double.parseDouble(ent[3]),Float.parseFloat(ent[4]),Float.parseFloat(ent[5]));
					String sai[] = plugin.getConfig().getString("Arena.Saida").split(";");
					plugin.saida = new Location(plugin.getServer().getWorld(sai[0]),Double.parseDouble(sai[1]),Double.parseDouble(sai[2]),Double.parseDouble(sai[3]),Float.parseFloat(sai[4]),Float.parseFloat(sai[5]));
					String cam[] = plugin.getConfig().getString("Arena.Camarote").split(";");
					plugin.camarote = new Location(plugin.getServer().getWorld(cam[0]),Double.parseDouble(cam[1]),Double.parseDouble(cam[2]),Double.parseDouble(cam[3]),Float.parseFloat(cam[4]),Float.parseFloat(cam[5]));
					sender.sendMessage("§6§l[MiniGlad] §oConfiguração recarregada!");
					return true;
				}
				sendHelp((Player)sender);
			}
			return true;
		}
		return true;
	}
	
	private void sendHelp(Player p) {
		p.sendMessage("§d§lPDGHMiniGlad - Comandos do plugin:");
		p.sendMessage("§2/miniglad ? -§a- Lista de comandos");
		p.sendMessage("§c/miniglad forcestart -§a- Força o inicio do evento miniglad");
		p.sendMessage("§c/miniglad forcestop -§a- Força a parada do evento miniglad");
		p.sendMessage("§2/miniglad kick <nome> -§a- Kicka um jogador do evento miniglad");
		p.sendMessage("§2/miniglad ban <nome> -§a- Bane um jogador do evento miniglad");
		p.sendMessage("§2/miniglad unban <nome> -§a- Desbane um jogador do evento miniglad");
		p.sendMessage("§2/miniglad setspawn -§a- Marca local de spawn do evento miniglad");
		p.sendMessage("§2/miniglad setsaida -§a- Marca local de saida do evento miniglad");
		p.sendMessage("§2/miniglad setcamarote -§a- Marca local do camarote do evento miniglad");
		p.sendMessage("§2/miniglad setcamarote -§a- Mostra quantos jogadores estão dentro do evento miniglad");
		p.sendMessage("§c/miniglad reload -§a- Recarrega a configuração");
	}

}
