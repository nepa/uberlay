// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: src/main/resources/upls.proto

package de.uniluebeck.itm.uberlay.protocols.upls;

public final class UPLS {
  private UPLS() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface UPLSPacketOrBuilder
      extends com.google.protobuf.MessageOrBuilder {
    
    // required uint32 label = 1;
    boolean hasLabel();
    int getLabel();
    
    // required bytes payload = 2;
    boolean hasPayload();
    com.google.protobuf.ByteString getPayload();
  }
  public static final class UPLSPacket extends
      com.google.protobuf.GeneratedMessage
      implements UPLSPacketOrBuilder {
    // Use UPLSPacket.newBuilder() to construct.
    private UPLSPacket(Builder builder) {
      super(builder);
    }
    private UPLSPacket(boolean noInit) {}
    
    private static final UPLSPacket defaultInstance;
    public static UPLSPacket getDefaultInstance() {
      return defaultInstance;
    }
    
    public UPLSPacket getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return de.uniluebeck.itm.uberlay.protocols.upls.UPLS.internal_static_de_uniluebeck_itm_uberlay_protocols_upls_UPLSPacket_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return de.uniluebeck.itm.uberlay.protocols.upls.UPLS.internal_static_de_uniluebeck_itm_uberlay_protocols_upls_UPLSPacket_fieldAccessorTable;
    }
    
    private int bitField0_;
    // required uint32 label = 1;
    public static final int LABEL_FIELD_NUMBER = 1;
    private int label_;
    public boolean hasLabel() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public int getLabel() {
      return label_;
    }
    
    // required bytes payload = 2;
    public static final int PAYLOAD_FIELD_NUMBER = 2;
    private com.google.protobuf.ByteString payload_;
    public boolean hasPayload() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public com.google.protobuf.ByteString getPayload() {
      return payload_;
    }
    
    private void initFields() {
      label_ = 0;
      payload_ = com.google.protobuf.ByteString.EMPTY;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      if (!hasLabel()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasPayload()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeUInt32(1, label_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, payload_);
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt32Size(1, label_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, payload_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacketOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return de.uniluebeck.itm.uberlay.protocols.upls.UPLS.internal_static_de_uniluebeck_itm_uberlay_protocols_upls_UPLSPacket_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return de.uniluebeck.itm.uberlay.protocols.upls.UPLS.internal_static_de_uniluebeck_itm_uberlay_protocols_upls_UPLSPacket_fieldAccessorTable;
      }
      
      // Construct using de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        label_ = 0;
        bitField0_ = (bitField0_ & ~0x00000001);
        payload_ = com.google.protobuf.ByteString.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket.getDescriptor();
      }
      
      public de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket getDefaultInstanceForType() {
        return de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket.getDefaultInstance();
      }
      
      public de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket build() {
        de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket buildPartial() {
        de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket result = new de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.label_ = label_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.payload_ = payload_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket) {
          return mergeFrom((de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket other) {
        if (other == de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket.getDefaultInstance()) return this;
        if (other.hasLabel()) {
          setLabel(other.getLabel());
        }
        if (other.hasPayload()) {
          setPayload(other.getPayload());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
        if (!hasLabel()) {
          
          return false;
        }
        if (!hasPayload()) {
          
          return false;
        }
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                onChanged();
                return this;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              label_ = input.readUInt32();
              break;
            }
            case 18: {
              bitField0_ |= 0x00000002;
              payload_ = input.readBytes();
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // required uint32 label = 1;
      private int label_ ;
      public boolean hasLabel() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public int getLabel() {
        return label_;
      }
      public Builder setLabel(int value) {
        bitField0_ |= 0x00000001;
        label_ = value;
        onChanged();
        return this;
      }
      public Builder clearLabel() {
        bitField0_ = (bitField0_ & ~0x00000001);
        label_ = 0;
        onChanged();
        return this;
      }
      
      // required bytes payload = 2;
      private com.google.protobuf.ByteString payload_ = com.google.protobuf.ByteString.EMPTY;
      public boolean hasPayload() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public com.google.protobuf.ByteString getPayload() {
        return payload_;
      }
      public Builder setPayload(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        payload_ = value;
        onChanged();
        return this;
      }
      public Builder clearPayload() {
        bitField0_ = (bitField0_ & ~0x00000002);
        payload_ = getDefaultInstance().getPayload();
        onChanged();
        return this;
      }
      
      // @@protoc_insertion_point(builder_scope:de.uniluebeck.itm.uberlay.protocols.upls.UPLSPacket)
    }
    
    static {
      defaultInstance = new UPLSPacket(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:de.uniluebeck.itm.uberlay.protocols.upls.UPLSPacket)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_de_uniluebeck_itm_uberlay_protocols_upls_UPLSPacket_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_de_uniluebeck_itm_uberlay_protocols_upls_UPLSPacket_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\035src/main/resources/upls.proto\022(de.unil" +
      "uebeck.itm.uberlay.protocols.upls\",\n\nUPL" +
      "SPacket\022\r\n\005label\030\001 \002(\r\022\017\n\007payload\030\002 \002(\014B" +
      "\010B\004UPLSH\001"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_de_uniluebeck_itm_uberlay_protocols_upls_UPLSPacket_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_de_uniluebeck_itm_uberlay_protocols_upls_UPLSPacket_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_de_uniluebeck_itm_uberlay_protocols_upls_UPLSPacket_descriptor,
              new java.lang.String[] { "Label", "Payload", },
              de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket.class,
              de.uniluebeck.itm.uberlay.protocols.upls.UPLS.UPLSPacket.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
