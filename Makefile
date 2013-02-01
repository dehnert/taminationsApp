
#  This file generates the xml files in the raw directory from
#  the corresponding files from the Taminations project.
#  Requires $(TAMINATIONS) to be set to the location of that project
#  Note that the images used in the definitions are not processed here -
#  if those are changed they need to be copied manually

OBJDIR = assets
SRC = $(wildcard $(TAMINATIONS)/*/*.xml) \
      $(wildcard $(TAMINATIONS)/*/*.html) \
      $(wildcard $(TAMINATIONS)/*/*.png)

SRCNAMES = $(subst $(TAMINATIONS),,$(SRC))
OBJ = $(addprefix $(OBJDIR),$(SRCNAMES))

all : $(OBJ)

$(OBJ) : $(SRC)

%.xml :
	copy $(subst /,\,$(subst $(OBJDIR),$(TAMINATIONS),$@)) $(subst /,\,$@)
%.html :
	copy $(subst /,\,$(subst $(OBJDIR),$(TAMINATIONS),$@)) $(subst /,\,$@)
%.png :
	copy $(subst /,\,$(subst $(OBJDIR),$(TAMINATIONS),$@)) $(subst /,\,$@)
