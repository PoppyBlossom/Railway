package com.railwayteam.railways.content.animated_flywheel;

import com.railwayteam.railways.config.CRConfigs;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

class FlywheelActorVisual extends ActorVisual {
	private static final double FLYWHEEL_DIAMETER = 2.8125; 

	private final RotatingInstance shaft;
	private final TransformedInstance wheel;
	private final Matrix4f baseTransform;

	private float angle;
	private float lastRenderTime;
	private boolean hasLastEntityPos;
	private double lastEntityX;
	private double lastEntityZ;

	FlywheelActorVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext context) {
		super(visualizationContext, simulationWorld, context);

		BlockState state = context.state;
		BlockPos localPos = context.localPos;
		@SuppressWarnings("null")
		Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);

		Instancer<RotatingInstance> shaftInstancer = instancerProvider.instancer(
			AllInstanceTypes.ROTATING,
			Models.partial(AllPartialModels.SHAFT)
		);
		this.shaft = shaftInstancer.createInstance();
		this.shaft
			.setRotationAxis(axis)
			.setRotationOffset(KineticBlockEntityVisual.rotationOffset(state, axis, localPos))
			.setPosition(localPos)
			.light(localBlockLight(), 0)
			.setChanged();

		Instancer<TransformedInstance> wheelInstancer = instancerProvider.instancer(
			InstanceTypes.TRANSFORMED,
			Models.partial(AllPartialModels.FLYWHEEL)
		);
		this.wheel = wheelInstancer.createInstance();

		@SuppressWarnings("null")
		Direction facing = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
		this.wheel
			.setIdentityTransform()
			.translate(localPos)
			.center()
			.rotate(new Quaternionf().rotateTo(0, 1, 0, facing.getStepX(), facing.getStepY(), facing.getStepZ()));

		this.baseTransform = new Matrix4f(this.wheel.pose);
		this.lastRenderTime = Float.NaN;
		this.hasLastEntityPos = false;

		applyWheelAngle(0);
	}

	@Override
	public void beginFrame() {
		float renderTime = AnimationTickHolder.getRenderTime();
		if (Float.isNaN(lastRenderTime))
			lastRenderTime = renderTime;

		float deltaTicks = renderTime - lastRenderTime;
		lastRenderTime = renderTime;
		if (deltaTicks < 0)
			deltaTicks = 0;

		float speedMultiplier = CRConfigs.client().flywheelSpeedMultiplier.getF();
		float rpm = computeRpm(deltaTicks) * speedMultiplier;
		float degreesPerTick = rpm * 360.0f / 1200.0f;
		this.angle = (this.angle + degreesPerTick * deltaTicks) % 360.0f;

		shaft.setRotationalSpeed(rpm).setChanged();
		applyWheelAngle(this.angle);
	}

	private float computeRpm(float deltaTicks) {
		if (!CRConfigs.client().animatedFlywheels.get())
			return 0;
		if (!(context.contraption instanceof CarriageContraption carriageContraption))
			return 0;
		if (!(carriageContraption.entity instanceof CarriageContraptionEntity carriageContraptionEntity))
			return 0;

		@SuppressWarnings("null")
		Direction.Axis axis = context.state.getValue(BlockStateProperties.AXIS);
		if (axis.isVertical())
			return 0;

		// Derive speed from position delta rather than Train.speed or getDeltaMovement().
		// - Train.speed can become stale client-side when control is lost (e.g. after collisions).
		// - getDeltaMovement() is often 0 for carriage contraptions because position is set directly.
		double trainSpeed = computeHorizontalSpeed(carriageContraptionEntity, deltaTicks);
		double circumference = Math.PI * FLYWHEEL_DIAMETER;
		if (circumference <= 0)
			return 0;

		double rpm = (trainSpeed / circumference) * 1200.0;
		if (carriageContraptionEntity.movingBackwards)
			rpm = -rpm;
		if (!Double.isFinite(rpm))
			return 0;

		return (float) rpm;
	}

	private double computeHorizontalSpeed(CarriageContraptionEntity entity, float deltaTicks) {
		if (deltaTicks <= 0)
			return 0;

		double x = entity.getX();
		double z = entity.getZ();
		if (!hasLastEntityPos) {
			hasLastEntityPos = true;
			lastEntityX = x;
			lastEntityZ = z;
			return 0;
		}

		double dx = x - lastEntityX;
		double dz = z - lastEntityZ;
		lastEntityX = x;
		lastEntityZ = z;

		return Math.sqrt(dx * dx + dz * dz) / deltaTicks;
	}

	private void applyWheelAngle(float angleDegrees) {
		wheel
			.setTransform(baseTransform)
			.rotateY(AngleHelper.rad(angleDegrees))
			.uncenter()
			.light(localBlockLight(), 0)
			.setChanged();
	}

	@Override
	protected void _delete() {
		shaft.delete();
		wheel.delete();
	}
}
