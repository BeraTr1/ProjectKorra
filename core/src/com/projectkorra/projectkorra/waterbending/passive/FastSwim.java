package com.projectkorra.projectkorra.waterbending.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms;

public class FastSwim extends WaterAbility implements PassiveAbility {

	private long cooldown;
	private double swimSpeed;
	private long duration;
	private boolean allowWaterArms;

	public FastSwim(final Player player) {
		super(player);
		if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		if (player.isSneaking()) { // the sneak event calls before they actually start sneaking
			return;
		}

		this.cooldown = ConfigManager.getConfig().getLong("Abilities.Water.Passive.FastSwim.Cooldown");
		this.swimSpeed = ConfigManager.getConfig().getDouble("Abilities.Water.Passive.FastSwim.SpeedFactor");
		this.duration = ConfigManager.getConfig().getLong("Abilities.Water.Passive.FastSwim.Duration");
		this.allowWaterArms = ConfigManager.getConfig().getBoolean("Abilities.Water.Passive.FastSwim.AllowWaterArms");

		this.start();
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canUsePassive(this) || !this.bPlayer.canBendPassive(this) || CoreAbility.hasAbility(this.player, EarthArmor.class)) {
			this.remove();
			return;
		}

		//Don't remove, for SpoutHop - avoids forcing SpoutHop users to re-sneak and recreate FastSwim (Less clunky)
		WaterSpout spout = CoreAbility.getAbility(player, WaterSpout.class);
		if (spout != null) {
			if (!spout.canSpoutHop()) {
				remove();
			}
			return;
		}
		
		if (CoreAbility.hasAbility(this.player, WaterArms.class) && !this.allowWaterArms) {
			this.remove();
			return;
		}

		if (this.duration > 0 && System.currentTimeMillis() > this.getStartTime() + this.duration) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}

		if (this.bPlayer.getBoundAbility() == null || (this.bPlayer.getBoundAbility() != null && !this.bPlayer.getBoundAbility().isSneakAbility())) {
			if (this.player.isSneaking()) {
				if (isWater(this.player.getLocation().getBlock()) && !this.bPlayer.isOnCooldown(this)) {
					GeneralMethods.setVelocity(this, this.player, this.player.getEyeLocation().getDirection().clone().normalize().multiply(this.swimSpeed));
				}
			} else {
				this.bPlayer.addCooldown(this);
				this.remove();
			}
		}
	}

	public static double getSwimSpeed() {
		return ConfigManager.getConfig().getDouble("Abilities.Water.Passive.FastSwim.SpeedFactor");
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public String getName() {
		return "FastSwim";
	}

	@Override
	public Location getLocation() {
		return this.player.getLocation();
	}

	@Override
	public boolean isInstantiable() {
		return false;
	}

	@Override
	public boolean isProgressable() {
		return true;
	}
}
