package net.lecousin.framework.ui;

public interface Images {
	static final String _path = "images/";

	public interface icons {
		static final String _path = Images._path+"icons/";
		
		public interface x7 {
			static final String _path = icons._path+"7x7/";
			
			public interface arrows {
				static final String _path = x7._path+"/arrows";
				
				public static final String COLLAPSE = _path+"collapse.gif";
				public static final String EXPAND= _path+"expand.gif";
			}
		}
		
		public interface x11 {
			static final String _path = icons._path+"11x11/";
			
			public interface arrows extends x7.arrows {
				static final String _path = x11._path+"arrows/";
				
				public static final String DOWN = _path+"down.gif";
				public static final String LEFT = _path+"left.gif";
				public static final String RIGHT = _path+"right.gif";
				public static final String UP = _path+"up.gif";
			}

			public interface basic {
				static final String _path = x11._path+"basic/";
				
				public static final String ADD = _path+"add.gif";
				public static final String DEL = _path+"del.gif";
				public static final String MOVE = _path+"move.gif";

				public static final String CLOSE_GRAY = _path+"close_gray.gif";
				
				public static final String IMPORT = _path+"import.gif";
				public static final String EXPORT = _path+"export.gif";
			}

			public interface dev {
				static final String _path = x11._path+"dev/";
				
				public static final String INHERITANCE = _path+"inheritance.gif";
				public static final String INPUT_PARAMETER = _path+"input_parameter.gif";
				public static final String SUBTYPE = _path+"subtype.gif";
			}
		}
		
		
		public interface x16 {
			static final String _path = icons._path+"16x16/";
			
			public interface arrows extends x11.arrows {
				static final String _path = x16._path+"arrows/";
				
				public static final String DOWN = _path+"down.gif";
				public static final String LEFT = _path+"left.gif";
				public static final String RIGHT = _path+"right.gif";
				public static final String UP = _path+"up.gif";

				public static final String COLLAPSE = _path+"collapse_doublearrow.gif";
				public static final String EXPAND = _path+"expand_doublearrow.gif";
			}

			public interface basic extends x11.basic {
				static final String _path = x16._path+"basic/";
				
				public static final String ADD = _path+"add.gif";
				public static final String DEL = _path+"del.gif";
				public static final String MOVE = _path+"move.gif";
				public static final String EDIT = _path+"edit.gif";
				public static final String HELP = _path+"help.gif";
				public static final String REFRESH = _path+"refresh.gif";
				public static final String SEARCH = _path+"search.gif";

				public static final String IMPORT = _path+"import.gif";
				public static final String EXPORT = _path+"export.gif";
				public static final String VALIDATE = _path+"validate.gif";
				public static final String VERSION = _path+"version.gif";

				public static final String ERROR = _path+"error.gif";
				public static final String WARNING = _path+"warning.gif";
				public static final String INFO = _path+"info.gif";
				public static final String OK = _path+"ok.gif";
				public static final String CANCEL = _path+"cancel.gif";
				public static final String IGNORE = _path+"ignore.png";
				public static final String CLOSE = CANCEL;
				
				public static final String INTERNET = _path+"internet.gif";

				public static final String PROCESSING = _path+"processing.gif";
				public static final String WAIT_CLOCK = _path+"wait_clock.gif";
				public static final String WAIT_ROUND = _path+"wait_round.gif";
				public static final String WAIT_ROUND_BAR = _path+"wait_round_bar.gif";
				
				public static final String HOME = _path+"home.gif";
				public static final String LABEL = _path+"label.gif";
				
				public static final String CALENDAR_POPUP = _path+"calendar_popup.gif";
				
				public static final String STAR_YELLOW_EMPTY = _path+"star_yellow_empty.gif";
				public static final String STAR_YELLOW_FULL = _path+"star_yellow_full.gif";
				public static final String STAR_YELLOW_QUART = _path+"star_yellow_quart.gif";
				public static final String STAR_YELLOW_HALF = _path+"star_yellow_half.gif";
				public static final String STAR_YELLOW_3QUART = _path+"star_yellow_3quart.gif";
				public static final String STAR_YELLOW_DISABLED = _path+"star_yellow_disabled.gif";
			}

			public interface dev extends x11.dev {
				static final String _path = x16._path+"dev/";
				
				public static final String PACKAGE = _path+"package.gif";
				public static final String CLASS = _path+"class.gif";
				public static final String INTERFACE = _path+"interface.gif";

				public static final String FIELD_DEFAULT = _path+"field_default.gif";
				public static final String FIELD_PUBLIC = _path+"field_public.gif";
				public static final String FIELD_PROTECTED = _path+"field_protected.gif";
				public static final String FIELD_PRIVATE = _path+"field_private.gif";
			}

			public interface file {
				static final String _path = x16._path+"file/";
				
				public static final String FOLDER_OPEN = _path+"folder_open.gif";
				public static final String FILE = _path+"file.gif";
				
				public static final String OPEN = _path+"open.gif";
				public static final String OPEN2 = _path+"open2.gif";
				public static final String OPEN_CLOSE = _path+"open_close.gif";
				public static final String SAVE = _path+"save.gif";
				public static final String NEW_FOLDER = _path+"new_folder.gif";
				
				public static final String FILE_TO_FILE = _path+"file_to_file.gif";
			}
			
			public interface filetypes {
				static final String _path = x16._path+"filetypes/";
				
				public static final String MOVIE = _path+"movie.gif";
				public static final String AUDIO = _path+"audio.gif";
			}
		}

		public interface x22 {
			static final String _path = icons._path+"22x22/";
			
			public interface basic extends x16.basic {
				static final String _path = x22._path+"basic/";
				
				public static final String SABLIER = _path+"sablier.gif";
			}
		}

		public interface x32 {
			static final String _path = icons._path+"32x32/";
			
			public interface basic extends x22.basic {
				static final String _path = x32._path+"basic/";
				
				public static final String ERROR = _path+"error.gif";
				public static final String WARNING = _path+"warning.gif";
				public static final String INFO = _path+"info.gif";
				public static final String QUESTION = _path+"question.gif";

				public static final String WAIT_ROUND = _path+"wait_round.gif";
			}
		}
		
		public interface x40 {
			static final String _path = icons._path+"40x40/";
			
			public interface basic extends x32.basic {
				static final String _path = x40._path+"basic/";
				
				public static final String WAIT_RECYCLE = _path+"wait_recycle.gif";
			}
		}
		
		public interface x48 {
			static final String _path = icons._path+"48x48/";
			
			public interface basic extends x40.basic {
				static final String _path = x48._path+"basic/";
				
				public static final String WAIT_ROUND_POINT = _path+"wait_round_point.gif";
			}
		}
		
		public interface x50 {
			static final String _path = icons._path+"50x50/";
			
			public interface basic extends x48.basic {
				static final String _path = x50._path+"basic/";
				
				public static final String WAIT_ROUND_BAR_STYLISH = _path+"wait_round_bar_stylish.gif";
			}
		}
		
		public interface tree {
			static final String _path = icons._path+"tree/";
			
			public static final String SUB_END = _path+"sub-end.gif";
			public static final String SUB_MORE = _path+"sub-more.gif";
		}
	}
	
}
