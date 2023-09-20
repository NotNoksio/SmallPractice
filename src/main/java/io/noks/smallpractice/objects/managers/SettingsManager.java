package io.noks.smallpractice.objects.managers;

import java.util.List;

import io.noks.smallpractice.abstracts.Settings;
import io.noks.smallpractice.objects.settings.DuelSetting;
import io.noks.smallpractice.objects.settings.MessageSetting;
import io.noks.smallpractice.objects.settings.PartySetting;
import io.noks.smallpractice.objects.settings.QueueSetting;
import io.noks.smallpractice.objects.settings.RequestDelaySetting;
import io.noks.smallpractice.objects.settings.ScoreboardSetting;
import net.minecraft.util.com.google.common.collect.Lists;

public class SettingsManager {
	private List<Settings> settings = Lists.newLinkedList();
	
	public SettingsManager() {
		this.settings.add(new QueueSetting());
		this.settings.add(new MessageSetting());
		this.settings.add(new PartySetting());
		this.settings.add(new DuelSetting());
		this.settings.add(new ScoreboardSetting());
		this.settings.add(new RequestDelaySetting());
	}
	public SettingsManager(QueueSetting queue, MessageSetting message, PartySetting party, DuelSetting duel, ScoreboardSetting scoreboard, RequestDelaySetting delay) {
		this.settings.add(queue);
		this.settings.add(message);
		this.settings.add(party);
		this.settings.add(duel);
		this.settings.add(scoreboard);
		this.settings.add(delay);
	}
	
	public Settings getSettingsByDisplayName(String displayName) {
		for (Settings settings : this.settings) {
			if (settings.getDisplayName().toLowerCase().equals(displayName)) {
				return settings;
			}
		}
		return null;
	}
}
