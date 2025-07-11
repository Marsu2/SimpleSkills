package org.simpleskills;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleskills.api.SkillsAPI;
import org.simpleskills.commands.*;
import org.simpleskills.manager.*;
import org.simpleskills.placeholder.SkillsPlaceholderExpansion;
import org.simpleskills.registry.SkillRegistry;
import org.simpleskills.upgrade.UpgradeRewardManager;

public class SimpleSkillsPlugin extends JavaPlugin implements CommandExecutor {
    private SkillManager skillManager;
    private static AttributeListener attributeListener;

    // ✅ Instance statique
    private static SimpleSkillsPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("SimpleSkillsPlugin: onEnable start");

        saveDefaultConfig();

        // reload registries
        SkillRegistry.reload(this);
        UpgradeRewardManager.reload(this);

        // init manager & API
        skillManager = new SkillManager(this);
        skillManager.loadAll();
        SkillsAPI.initialize(skillManager);

        // listeners
        attributeListener = new AttributeListener(skillManager);
        getServer().getPluginManager().registerEvents(attributeListener, this);
        getServer().getPluginManager().registerEvents(new InventoryListener(skillManager), this);
        getServer().getPluginManager().registerEvents(new AgilitySpeedEffectListener(skillManager), this);
        getServer().getPluginManager().registerEvents(new EventWorldSkillResetListener(skillManager, getDataFolder()), this);

        // commands
        getCommand("skills").setExecutor(new SkillsMenuCommand(skillManager));
        getCommand("giveSkills").setExecutor(new GivePointsCommand(skillManager));
        getCommand("sellall").setExecutor(new SellCommand());
        getCommand("skillsreload").setExecutor(new SkillsReloadCommand(this));

        SkillsAdminCommand adminCmd = new SkillsAdminCommand(skillManager);
        getCommand("skillsadmin").setExecutor(adminCmd);
        getCommand("skillsadmin").setTabCompleter(new SkillsAdminTabCompleter());

        new SkillsPlaceholderExpansion().register();
        getLogger().info("SimpleSkillsPlugin: onEnable complete");
    }

    @Override
    public void onDisable() {
        if (skillManager != null) skillManager.saveAll();
    }

    public static AttributeListener getAttributeListener() {
        return attributeListener;
    }

    // ✅ Accès statique au SkillManager
    public static SkillManager getSkillManager() {
        return instance.skillManager;
    }
    public static SimpleSkillsPlugin getInstance() {
        return instance;
    }
}
