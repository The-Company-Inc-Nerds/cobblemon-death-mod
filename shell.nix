{
  pkgs,
  inputs,
}: let
  zedSettings = {
    lsp = {
      nix = {
        binary = {
          path_lookup = true;
        };
      };
      nil = {
        initialization_options = {
          formatting = {
            command = [
              "alejandra"
              "--quiet"
              "--"
            ];
          };
        };
      };
      nixd = {
        initialization_options = {
          formatting = {
            command = [
              "alejandra"
              "--quiet"
              "--"
            ];
          };
        };
      };
    };

    auto_install_extensions = {
      "nix" = true;
      "java" = true;
      "kotlin" = true;
    };

    languages = {
      nix = {
        formatter = {
          external = {
            command = "alejandra";
            arguments = [
              "--quiet"
              "--"
            ];
          };
        };
      };
    };
  };
in
  pkgs.mkShell {
    buildInputs = [
      (inputs.zed-editor.packages.x86_64-linux.default zedSettings)
      pkgs.jdk21
      pkgs.gradle
      pkgs.kotlin
    ];

    JAVA_HOME = "${pkgs.jdk21}";
    GRADLE_USER_HOME = ".gradle";

    shellHook = ''
      echo "Using wrapped local zed: ./.direnv/.config/zed"
      echo "Java version: $(java -version 2>&1 | head -n 1)"
      echo "JAVA_HOME: $JAVA_HOME"
    '';
  }
