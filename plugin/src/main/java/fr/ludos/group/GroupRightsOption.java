package fr.ludos.group;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum GroupRightsOption {
	none,
	invite {
		@Override
		public boolean canInvite() {
			return true;
		}
	},
	game {
		@Override
		public boolean canInvite() {
			return true;
		}
		@Override
		public boolean canRunGames() {
			return true;
		}
	},
	config {
		@Override
		public boolean canInvite() {
			return true;
		}
		@Override
		public boolean canRunGames() {
			return true;
		}
		@Override
		public boolean canConfig() {
			return true;
		}
	},
	all {
		@Override
		public boolean canInvite() {
			return true;
		}
		@Override
		public boolean canRunGames() {
			return true;
		}
		@Override
		public boolean canConfig() {
			return true;
		}
		@Override
		public boolean canManage() {
			return true;
		}
	};

	public boolean canInvite() {
		return false;
	}
	public boolean canRunGames() {
		return false;
	}
	public boolean canConfig() {
		return false;
	}
	public boolean canManage() {
		return false;
	}

	public static List<String> getOptions() {
		return Arrays.stream(GroupRightsOption.values())
			.map(GroupRightsOption::toString)
			.collect(Collectors.toList());
	}

	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(GroupRightsOption.values()).map(GroupRightsOption::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append(">");

		return sb.toString();
	}
}
